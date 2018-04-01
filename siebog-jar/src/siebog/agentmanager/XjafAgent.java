/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package siebog.agentmanager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.client.SiebogNode;
import siebog.connectionmanager.ObjectField;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.MessageManager;
import siebog.messagemanager.Performative;
import siebog.util.NodeManager;
import siebog.util.ObjectFactory;

/**
 * Base class for all agents.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 */
@Lock(LockType.READ)
public abstract class XjafAgent implements Agent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(XjafAgent.class);
	// the access timeout is needed only when the system is under a heavy load.
	// under normal circumstances, all methods should return as quickly as
	// possible
	public static final long ACCESS_TIMEOUT = 5;
	/**
	 * Agent's AID.
	 */
	protected AID myAid;
	
	private AgentManager agm;
	private MessageManager msm;

	// TODO : Restore support for heartbeats.
	// private transient long hbHandle;

	/**
	 * Called when the agent is created. Calls the onInit callback.
	 */
	@Override
	public void init(AID aid, AgentInitArgs args) {
		myAid = aid;
		onInit(args);
	}

	/**
	 * Override this to react when the agent is initialized.
	 * @param args
	 */
	protected void onInit(AgentInitArgs args) {
	}

	/**
	 * Handles ACL messages which have arrived to the agent.
	 * First tries to filter out messages, and then 
	 * calles the abstract onMesssge method (which needs to be overriden).
	 */
	@Override
	public void handleMessage(ACLMessage msg) {
		// TODO : check if the access to onMessage is protected
		if (filter(msg)) {
			try {
				onMessage(msg);
			} catch (Exception ex) {
				LOG.warn("Error while delivering message {}.", msg, ex);
			}
		}
	}

	/**
	 * Override this to react when some ACL message arrives to your agent.
	 * 
	 * @param msg
	 */
	protected abstract void onMessage(ACLMessage msg);

	/**
	 * Heartbeat not implemented.
	 * @param content
	 * @return
	 */
	protected boolean onHeartbeat(String content) {
		return false;
	}

	/**
	 * Overrride this to put some code which will be called before agent's termination.
	 */
	protected void onTerminate() {
	}

	/**
	 * Called before an agent is stopped.
	 */
	@Override
	@Remove
	public void stop() {
		try {
			onTerminate();
		} catch (Exception ex) {
			LOG.warn("Error in onTerminate.", ex);
		}
	}

	/**
	 * Not implemented.
	 * @return
	 */
	protected ACLMessage receiveNoWait() {
		return null; // queue.poll(); // TODO : Implement receiveNoWait.
	}

	/**
	 * Not implemented.
	 * 
	 * @param timeout
	 * @return
	 */
	protected ACLMessage receiveWait(long timeout) {
		if (timeout < 0)
			throw new IllegalArgumentException("The timeout value cannot be negative.");
		ACLMessage msg = null;
		// TODO : Implement receiveWait.
		// try {
		// if (timeout == 0)
		// timeout = Long.MAX_VALUE;
		// msg = queue.poll(timeout, TimeUnit.MILLISECONDS);
		// } catch (InterruptedException ex) {
		// }
		return msg;
	}

	@Override
	public int hashCode() {
		return myAid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return myAid.equals(((XjafAgent) obj).myAid);
	}

	/**
	 * Before being finally delivered to the agent, the message will be passed
	 * to this filtering function.
	 * 
	 * @param msg
	 * @return If false, the message will be discarded.
	 */
	protected boolean filter(ACLMessage msg) {
		return true;
	}

	public AID getAid() {
		return myAid;
	}

	protected String getNodeName() {
		return NodeManager.getNodeName();
	}

	@Override
	public String ping() {
		return getNodeName();
	}

	protected AgentManager agm() {
		if (agm == null)
			agm = ObjectFactory.getAgentManager(SiebogNode.LOCAL);
		return agm;
	}

	protected MessageManager msm() {
		if (msm == null)
			msm = ObjectFactory.getMessageManager(SiebogNode.LOCAL);
		return msm;
	}

	public void move(String host) {
		List<ObjectField> list = this.deconstruct();
		agm().move(this.myAid, host, list);
	}
	
	/**
	 * Used when server-side agent arrives to another JBoss node. 
	 * It actually de-serializes agent's internal state. 
	 */
	public void reconstruct(List<ObjectField> agentFields) {
		Class<?> agentClass = this.getClass();
		for (ObjectField field : agentFields) {
			try {
				agentClass.getMethod("set" + field.getName(), field.getType()).invoke(this, field.getValue());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		ACLMessage message = new ACLMessage(Performative.RESUME);
		message.receivers.add(myAid);
		message.content = getNodeName();
		msm().post(message);
	}

	/**
	 * Used before server-side agent starts moving to another JBoss node.
	 * It actually serializes agent's internal state. 
	 */
	public List<ObjectField> deconstruct() {
		List<ObjectField> retVal = new ArrayList<>();
		Class<?> agentClass = this.getClass();
		Method[] methods = agentClass.getMethods();
		for (Method method : methods) {
			if (!method.getName().equals("getClass") && method.getName().startsWith("get")) {
				try {
					retVal.add(new ObjectField(method.getName().substring(3), method.getReturnType(),
							method.invoke(this)));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return retVal;
	}
}