package siebog.agents.test;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import siebog.agentmanager.Agent;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import util.LoggerUtil;

@Stateless
@Remote(Agent.class)
public class TestAgent extends XjafAgent {

	private static final long serialVersionUID = -3042040725633126285L;

	@Override
	protected void onMessage(ACLMessage msg) {
		LoggerUtil.log("TEST AGENT RECEIVED A MESSAGE: " + msg, true);
	}
	

}
