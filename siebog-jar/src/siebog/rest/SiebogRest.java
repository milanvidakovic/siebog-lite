package siebog.rest;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import siebog.agentmanager.AID;
import siebog.agentmanager.AgentClass;
import siebog.messagemanager.ACLMessage;

public interface SiebogRest {

	@GET
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AgentClass> getAvailableAgentClasses();

	@PUT
	@Path("/run/{name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AID startServerAgent(AgentClass agClass, @PathParam("name") String runtimeName);

	@GET
	@Path("/running")
	public List<AID> getRunningAgents();

	@DELETE
	@Path("/stop")
	@Consumes(MediaType.APPLICATION_JSON)
	public void stopAgent(AID aid);

	@GET
	@Path("/performatives")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getPerformatives();
	
	@GET
	@Path("/hostname")
	@Produces(MediaType.TEXT_PLAIN)
	public String getHostName();
	
	@PUT
	@Path("/acceptRadigostAgent")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String acceptRadigostAgent(String agentState, @Context ServletContext ctx);

	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	public void post(ACLMessage msg);	
	
}
