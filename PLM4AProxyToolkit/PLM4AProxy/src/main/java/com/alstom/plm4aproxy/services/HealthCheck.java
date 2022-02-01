
package com.alstom.plm4aproxy.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class handle Health Check.
 * @author Accenture
 * @version 1.0
 *
 */
@Path("/tools")
public class HealthCheck {
	
	public static final String VERSION = "1.1.0";
	
	@GET
	@Path("healthcheck")
	@Produces(MediaType.APPLICATION_JSON)
	public Response check() {
		return Response.ok("{\"status\":\"UP\"}").build();
	}
	

	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	public Response version() {
		return Response.ok("{\"version\":\""+VERSION+"\"}").build();
	}
}
