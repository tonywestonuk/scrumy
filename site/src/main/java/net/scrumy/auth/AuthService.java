package net.scrumy.auth;

import java.util.Map;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/")
@RegisterRestClient
public interface AuthService {

	@POST
	@Path("/token")
	@ClientHeaderParam(name="Content-Type", value="application/x-www-form-urlencoded")
    public Map<String,String> token(
			@FormParam("grant_type") String grantType,
			@FormParam("client_id") String clientID,
			@FormParam("client_secret") String clientSecret,
			@FormParam("code") String code,
			@FormParam("redirect_uri") String redirectURI
			);

	@POST
	@Path("/token")
	@ClientHeaderParam(name="Content-Type", value="application/x-www-form-urlencoded")
    public Map<String,String> refresh(
			@FormParam("grant_type") String grantType,
			@FormParam("client_id") String clientID,
			@FormParam("client_secret") String clientSecret,
			@FormParam("refresh_token") String refreshToken
			);
	
	@GET
	@Path("/certs")
	public Map<String,Cert[]> certs();
	
	public static record Cert(String kid, String use, String[] x5c) {};
}
