package siebog.messagemanager;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

public class TimerObjectMessage implements Serializable {

	private static final long serialVersionUID = -6414124551893629548L;
	
	public String JMSXGroupID;
	public int AIDIndex;
	public String _HQ_DUPL_ID;
	public ACLMessage message;

	public TimerObjectMessage(ObjectMessage jmsMsg) {
		try {
			this.JMSXGroupID = jmsMsg.getStringProperty("JMSXGroupID");
			this.AIDIndex = jmsMsg.getIntProperty("AIDIndex");
			this._HQ_DUPL_ID = jmsMsg.getStringProperty("_HQ_DUPL_ID");
			this.message = (ACLMessage) jmsMsg.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
