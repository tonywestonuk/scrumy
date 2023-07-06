package net.scrumy.controllers;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class Homepage {

	
	 @Inject
     Template index;
	
	
	@GET
	@Path("/")
    @Produces(MediaType.TEXT_HTML)
	public TemplateInstance getIndex() {		
		TemplateInstance rtn = index.instance();		
		return rtn;
	}
}
