package net.scrumy.services;


import net.scrumy.auth.AuthController.UserCredentials;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@RequestScoped
@Named("user")
public class UserService {

	private boolean loggedIn = false;
	
	private UserCredentials credentials;

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public UserCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(UserCredentials credentials) {
		this.credentials = credentials;
	}
	
	

}
