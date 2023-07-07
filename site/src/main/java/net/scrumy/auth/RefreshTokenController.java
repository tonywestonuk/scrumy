package net.scrumy.auth;

import java.net.URI;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

@Path("/auth/refreshToken")
public class RefreshTokenController {
	
	@RestClient
	AuthService authService;
	
	@GET
	public Response get(Request rqs, @QueryParam("token") String refreshToken, @QueryParam("redirectTo") String redirectTo) {
		
		Map<String,String> token;
		try {
		  token = authService.refresh(
				"refresh_token",
				"scrumy",
				"uw32aDWvYmmzQhCm8cDxiF4rTGu7zQgb",
				refreshToken);
		} catch (Exception e) {
			NewCookie cookie_rt =new NewCookie.Builder("refresh_token")
				.maxAge(0)
				.path("/")
				.httpOnly(true)
				.build();
			return Response.
					temporaryRedirect(URI.create(redirectTo))
					.cookie(cookie_rt)
					.build();
		}
	
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
				temporaryRedirect(URI.create(redirectTo))
				.cookie(cookie_at,cookie_rt)
				.build();
	}
}
