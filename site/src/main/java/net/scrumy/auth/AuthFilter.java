package net.scrumy.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import net.scrumy.auth.AuthController.UserCredentials;
import net.scrumy.auth.AuthService.Cert;
import net.scrumy.services.UserService;

@ApplicationScoped
public class AuthFilter {

	@RestClient
	AuthService authService;

	@Inject
	ManagedExecutor executor;
	
	@Inject
	UserService userService;
	
	
	long maintenanceTime=0;


	private PublicKey publicKey;
	
	private ConcurrentHashMap<String, Date> expiredSessions = new ConcurrentHashMap<>();
	

	@ServerRequestFilter(preMatching = true)
	public void filter(ContainerRequestContext requestContext) throws IOException {

		if (requestContext.getUriInfo().getPath().startsWith("/auth"))
			return;
		
		maintenence();
		
		// Read and validate auth cookies.
		var cookies = requestContext.getCookies();
		var authenticationTokenCookie = cookies.get("access_token");
		var refreshTokenCookie = cookies.get("refresh_token");

		if (authenticationTokenCookie != null) {
			String[] accessToken = authenticationTokenCookie.getValue().split("\\.");
			
			// validate..
			if (verifySig(accessToken[0], accessToken[1], accessToken[2]) == true) {				
				byte[] credData = Base64.getDecoder().decode(accessToken[1]);
				ObjectMapper om = new ObjectMapper();
				om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				UserCredentials cred = om.readValue(credData, UserCredentials.class);
				
				if (expiredSessions.containsKey(cred.sid())) {
					// need to logout token...
					String relURL = requestContext.getUriInfo().getRequestUri().toString()
							.substring(requestContext.getUriInfo().getBaseUri().toString().length() - 1);

					String encodedURL = URLEncoder.encode(relURL, Charset.defaultCharset());
					requestContext.setRequestUri(URI.create("/auth/logout?redirectTo=" + encodedURL));
					return;
				}
				
				userService.setCredentials(cred);
				userService.setLoggedIn(true);
				return;
			}
		}

		if (refreshTokenCookie != null) {
			String relURL = requestContext.getUriInfo().getRequestUri().toString()
					.substring(requestContext.getUriInfo().getBaseUri().toString().length() - 1);

			// need to refresh token...
			requestContext.setRequestUri(
						URI.create("/auth/refreshToken?token=" + refreshTokenCookie.getValue()
						+ "&redirectTo=" + URLEncoder.encode(relURL
							, Charset.defaultCharset())

			));
			return;
		}

		// get here, then user is not logged on.
		userService.setLoggedIn(false);	
	}

	public boolean verifySig(String headerEncoded, String payloadEncoded, String signatureEncoded) {
		// Dont verify, if haven't retrieved the public key (yet).
		if (publicKey == null)
			return true;

		String signatureCreatedFromThisData = headerEncoded + "." + payloadEncoded;
		Signature publicSignature;
		try {
			publicSignature = Signature.getInstance("SHA256withRSA");
			publicSignature.initVerify(publicKey);
			publicSignature.update(signatureCreatedFromThisData.getBytes());
			return publicSignature.verify(Base64.getUrlDecoder().decode(signatureEncoded));

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void addLoggedOutSession(String session) {
		expiredSessions.put(session, new Date());
	}

	
	public synchronized void maintenence() {
		
		if (System.currentTimeMillis()- maintenanceTime<30000) return;   // Do every 30 seconds.
		maintenanceTime=System.currentTimeMillis();
		
		System.out.println("submitting maintenence");

		executor.execute(() -> {
			
			System.out.println("doing maintenence");
			
			Iterator<Entry<String,Date>>  iter = expiredSessions.entrySet().iterator();
			while (iter.hasNext()) {
				var entry = iter.next();
					if ((System.currentTimeMillis() - entry.getValue().getTime())>(1800*1000)) {
						iter.remove();
					}
			}
			
			if (publicKey!=null) return;
			
			System.out.println("fetching public key");
			var _certs = authService.certs();
			for (Cert c : _certs.get("keys"))
				if (c.use().equals("sig")) {
					try {
						System.out.println("loaded public key");
						publicKey = CertificateFactory.getInstance("X.509")
								.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(c.x5c()[0])))
								.getPublicKey();
					} catch (CertificateException e) {
						e.printStackTrace();
					}

				}
		});
	}
}
