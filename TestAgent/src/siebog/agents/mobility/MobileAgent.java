package siebog.agents.mobility;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import siebog.agentmanager.Agent;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import util.LoggerUtil;

@Stateless
@Remote(Agent.class)
public class MobileAgent extends XjafAgent {

	private static final long serialVersionUID = -772277829600909136L;

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			LoggerUtil.log("Received message to move to host: " + msg.content);
			move(msg.content);
		} else if (msg.performative == Performative.RESUME) {
			LoggerUtil.log("Arrived at host: " + msg.content, true);
		} else {
			LoggerUtil.log("Wrong performative. Send me REQUEST to move to other node.", true);
		}
		
	}

}
