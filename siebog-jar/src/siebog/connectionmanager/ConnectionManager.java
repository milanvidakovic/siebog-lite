package siebog.connectionmanager;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface ConnectionManager {

	@POST
	@Path("/new")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<String> newConnection(String connection);
	
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addConnection(String connection);
	
	@POST
	@Path("/move")
	@Consumes(MediaType.APPLICATION_JSON)
	public void moveAgent(List<ObjectField> agent);

}
