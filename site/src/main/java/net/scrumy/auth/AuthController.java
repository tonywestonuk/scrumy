package net.scrumy.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthController {
	
	@RestClient
	AuthService authService;
	
	@GET
	@Path("/")
	public Response auth(
			@HeaderParam("host") String host,
			@QueryParam("session_state") String sessionState,
			@QueryParam("code") String code) throws StreamReadException, DatabindException, IOException {
				
		String redirectURL = "https://www.scrumy.net/auth";
		
		if (host.equals("localhost:8080")){
			redirectURL = "http://localhost:8080/auth";
		}
		
				
		var token= authService.token(
					"authorization_code", 
					"scrumy" , 
					"uw32aDWvYmmzQhCm8cDxiF4rTGu7zQgb", 
					code,
					redirectURL);

		NewCookie cookie_at = new NewCookie.Builder("access_token")
					.value(token.get("access_token"))
					.maxAge(Integer.parseInt(token.get("expires_in")))
					.path("/")
					.httpOnly(true)
					.build();
		
		NewCookie cookie_rt = new NewCookie.Builder("refresh_token")
				.value(token.get("refresh_token"))
				.maxAge(Integer.parseInt(token.get("refresh_expires_in")))
				.path("/")
				.httpOnly(true)
				.build();

		return Response.
				temporaryRedirect(URI.create("/"))
				.cookie(cookie_at,cookie_rt)
				.build();
	}
	
	
	public static record UserCredentials(
			@JsonProperty("sub") UUID userID, String sid, String name, String email, String preferred_username) {
		
	};

}
