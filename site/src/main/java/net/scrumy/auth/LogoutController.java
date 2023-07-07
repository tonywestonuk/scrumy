package net.scrumy.auth;

import java.net.URI;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/auth/logout")
public class LogoutController {
	
	@Inject
	AuthFilter authFilter;
	
	
	@GET
	public Response logoutGet(@QueryParam("redirectTo") @DefaultValue("/") String redirectTo) {
		NewCookie cookie_at = new NewCookie.Builder("access_token")
				.value("")
				.maxAge(0)
				.path("/")
				.httpOnly(true)
				.build();
	
		NewCookie cookie_rt = new NewCookie.Builder("refresh_token")
				.value("")
				.maxAge(0)
				.path("/")
				.httpOnly(true)
				.build();

		return Response.
				temporaryRedirect(URI.create(redirectTo))
				.cookie(cookie_at,cookie_rt)
				.build();
	}
	
	@POST
	public void logoutPost(String body) throws JsonMappingException, JsonProcessingException {
		String logoutToken[] = body.substring(13).split("\\.");
		String tokenBody = new String(Base64.getDecoder().decode(logoutToken[1]));
		
		if (authFilter.verifySig(logoutToken[0], logoutToken[1], logoutToken[2])==false) return;
		
		
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		LogoutToken lot = om.readValue(tokenBody, LogoutToken.class);
		
		authFilter.addLoggedOutSession(lot.sid());
	}
	
	public static record  LogoutToken(String sid) {};
	
	
}
