package net.scrumy.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("messageService")
public class MessageService {
	
	private ArrayList<Msg> messages = new ArrayList<>();
	
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
	

	public void postMessage(String userid, String username, String message) {
		Msg msg = new Msg(userid, username, message,LocalTime.now().format(dtf));
		messages.add(0,msg);
		if (messages.size()>100) messages.remove(100);
	}
	
	public static record Msg(String userid, String username, String msg, String date) {}; 
	
	
	public List<Msg> getMessages(){
		System.out.println("poo" + messages.size());
		return messages;
	}
}
