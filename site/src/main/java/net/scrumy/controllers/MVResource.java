package net.scrumy.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/img/scrum.jpg")
public class MVResource {

	@GET
	@Produces("image/jpg")
	public byte[] staticResource(String string) {
			try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/img/scrumy.jpg")) {
				
				
				byte[] bytes = is.readAllBytes();
				return bytes;
			} catch (IOException e) {
				
				throw new RuntimeException("Unable to load "+string,e);
			}
	}
}
