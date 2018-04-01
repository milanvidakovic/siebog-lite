package siebog.messagemanager;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentManagerBean;

/**
 * Central hub for all ACL messages which are sent inside Siebog. Every ACL message is first placed inside the 
 * jms/queue/siebog queue, which in turn fires the onMessage method of this class.
 * 
 * @author Minja
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/siebog")
})
public class MDBConsumer implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(MDBConsumer.class);

	@EJB
	private AgentManagerBean agm;

	@Override
	public void onMessage(Message msg) {
		try {
			processMessage(msg);
		} catch (JMSException ex) {
			LOG.warn("Cannot process an incoming message.", ex);
		}
	}

	/**
	 * Processes a message. Tries to extract AID of the recipient, and then delivers it.
	 * @param msg JMS message which holds the ACL message.
	 * @throws JMSException
	 */
	private void processMessage(Message msg) throws JMSException {
		ACLMessage acl = (ACLMessage) ((ObjectMessage) msg).getObject();
		AID aid = getAid(msg, acl);
		deliverMessage(acl, aid);
	}
	
	/**
	 * Returns the recipient's AID from the message.
	 * 
	 * @param msg JMS message which holds the ACL message.
	 * @param acl ACL message extracted from the JMS message.
	 * @return
	 * @throws JMSException
	 */
	private AID getAid(Message msg, ACLMessage acl) throws JMSException {
		int i = msg.getIntProperty("AIDIndex");
		return acl.receivers.get(i);
	}

	/**
	 * Delivers the ACL message to the agent who is identified by its AID.  
	 * 
	 * @param msg
	 * @param aid
	 */
	private void deliverMessage(ACLMessage msg, AID aid) {
		Agent agent = agm.getAgentReference(aid);
		if (agent != null) {
			agent.handleMessage(msg);
		} else {
			LOG.info("No such agent: {}", aid.getName());
		}
	}
}
