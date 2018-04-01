package siebog.agents.pingpong;

import siebog.agentmanager.AID;
import siebog.agentmanager.AgentClass;
import siebog.client.SiebogClient;
import siebog.client.SiebogNode;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.util.ObjectFactory;

/**
 * 
 * @author <a href="mvidakovic@gmail.com">Milan Vidakovic</a>
 */
public class Test {
	private static SiebogNode node;
	
	public static void main(String[] args) {
		node = new SiebogNode("localhost", 8080);
		SiebogClient.connect(node);
		AID pingAgent = createPingAgent();
		createPongAgent();
		start(pingAgent);
	}

	private static AID createPingAgent() {
		AgentClass cls = new AgentClass("TestAgent",
				Ping.class.getSimpleName());
		return ObjectFactory.getAgentManager(node).startServerAgent(cls, "ping");
	}

	private static void createPongAgent() {
		AgentClass cls = new AgentClass("TestAgent",
				Pong.class.getSimpleName());
		ObjectFactory.getAgentManager(node).startServerAgent(cls, "pong");
	}

	private static void start(AID pingAgent) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(pingAgent);
		msg.content = "pong";
		ObjectFactory.getMessageManager(node).post(msg);
	}
}
