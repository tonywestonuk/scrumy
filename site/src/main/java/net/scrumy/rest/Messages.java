package net.scrumy.rest;

import java.time.LocalDate;

import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.scrumy.services.MessageService;
import net.scrumy.services.UserService;

@Path("/rest/messages")
public class Messages {

	@Inject
	UserService userService;

	@Inject
	MessageService msgService;

	@POST
	public void postMessage(@FormParam("msg") String msg) {
		System.out.println(msg);
		System.out.println(userService.getCredentials().preferred_username());

		msgService.postMessage(userService.getCredentials().userID().toString(),
				userService.getCredentials().preferred_username(), msg);
	}
}
