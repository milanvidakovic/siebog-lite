package siebog.connectionmanager;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.AgentManager;
import siebog.util.FileUtils;
import siebog.util.NodeManager;

@Singleton
@Startup
@Remote(ConnectionManager.class)
@Path("/connection")
public class ConnectionManagerBean implements ConnectionManager {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionManagerBean.class);
	
	private String nodeAddr;
	private String nodeName;
	private String master = null;
	private List<String> connections = new ArrayList<String>();

	@EJB
	private AgentManager agm;

	@PostConstruct
	private void init() {
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");
			this.nodeAddr = (String) mBeanServer.getAttribute(http, "boundAddress");
			this.nodeName = NodeManager.getNodeName() + ":8080";

			File f = FileUtils.getFile(ConnectionManager.class, "", "connections.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			this.master = properties.getProperty("master");
			LOG.info("MASTER ADDR: " + master + ", node name: " + nodeName + ", node address: " + nodeAddr);
			if (master != null && !master.equals("")) {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + master + "/siebog-war/rest/connection");
				ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
				this.connections = rest.newConnection(this.nodeName);
				this.connections.remove(this.nodeName);
				this.connections.add(this.master);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNodeName() {
		return this.nodeName;
	}

	@Override
	public List<String> newConnection(String connection) {
		LOG.info("New node registered: " + connection);
		for (String c : connections) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + c + "/siebog-war/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.addConnection(connection);
		}
		connections.add(connection);
		return connections;
	}

	@Override
	public void addConnection(String connection) {
		connections.add(connection);

	}

	@Override
	public void moveAgent(List<ObjectField> agent) {
		agm.reconstructAgent(agent);

	}

}
