package siebog.agents.test;

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
		AID testAgent = createTestAgent();
		start(testAgent);
	}

	private static AID createTestAgent() {
		AgentClass cls = new AgentClass("TestAgent",
				TestAgent.class.getSimpleName());
		return ObjectFactory.getAgentManager(node).startServerAgent(cls, "test");
	}

	private static void start(AID testAgent) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(testAgent);
		msg.content = "test message content";
		ObjectFactory.getMessageManager(node).post(msg);
	}
}
