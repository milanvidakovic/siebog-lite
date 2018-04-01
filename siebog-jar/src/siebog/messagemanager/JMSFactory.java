package siebog.messagemanager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.Agent;

@Singleton
@LocalBean
/**
 * Used for JMS lookup.
 * 
 * @author Dejan 
 * @author Minja
 *
 */
public class JMSFactory {
	private Logger LOG = LoggerFactory.getLogger(JMSFactory.class);
	private Connection connection;
	@Resource(lookup = "java:jboss/exported/jms/RemoteConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(lookup = "java:jboss/exported/jms/queue/siebog")
	private Queue defaultQueue;
	@Resource(lookup = "java:jboss/exported/jms/queue/siebog")
	private Queue testQueue;

	@PostConstruct
	public void postConstruct() {
		try {
			//connection = connectionFactory.createConnection(siebog.nodemanager.Global.USERNAME, siebog.nodemanager.Global.PASSWORD);
			connection = connectionFactory.createConnection("guest", "guest.guest.1");
			connection.setClientID(Agent.SIEBOG_MODULE);
			connection.start();
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@PreDestroy
	public void preDestroy() {
		try {
			connection.close();
		} catch (JMSException ex) {
			LOG.warn("Exception while closing the JMS connection.", ex);
		}
	}

	public Session getSession() {
		try {
			return connection.createSession(false, QueueSession.AUTO_ACKNOWLEDGE);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public MessageProducer getDefaultProducer(Session session) {
		try {
			return session.createProducer(defaultQueue);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public MessageProducer getTestProducer(Session session) {
		try {
			return session.createProducer(testQueue);
		} catch (JMSException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
