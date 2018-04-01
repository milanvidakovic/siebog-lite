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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import siebog.client.SiebogNode;
import siebog.connectionmanager.ConnectionManager;
import siebog.connectionmanager.ObjectField;
import siebog.util.GlobalCache;
import siebog.util.JndiTreeParser;
import siebog.util.NodeManager;
import siebog.util.ObjectFactory;
import util.LoggerUtil;
import util.LoggerUtil.SocketMessageType;

/**
 * Default agent manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */
@Stateless
@Remote(AgentManager.class)
@LocalBean
public class AgentManagerBean implements AgentManager {
	private static final long serialVersionUID = 1L;
	/**
	 * Infinispan cache of agents.
	 */
	private Cache<AID, Agent> agents;

	/**
	 * Used to find all server-side agents inside the JNDI. 
	 */
	@EJB
	private JndiTreeParser jndiTreeParser;
	
	@EJB
	ConnectionManager com;

	/**
	 * Returns the list of all available agent classes. AgentClass just describes an agent.
	 */
	@Override
	public List<AgentClass> getAvailableAgentClasses() {
		try {
			return jndiTreeParser.parse();
		} catch (NamingException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
	/**
	 * Starts a server-side agent.
	 */
	@Override
	public void startServerAgent(AID aid, AgentInitArgs args, boolean replace) {
		if (getCache().containsKey(aid)) {
			if (!replace) {
				throw new IllegalStateException("Agent already running: " + aid);
			}
			stopAgent(aid);
			if(args == null || args.get("noUIUpdate", "").equals("")) {
				LoggerUtil.logAgent(aid, SocketMessageType.REMOVE);
			}
		}
		Agent agent = null;
		try {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), true), Agent.class, SiebogNode.LOCAL);
		} catch (IllegalStateException ex) {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), false), Agent.class, SiebogNode.LOCAL);
		}
		initAgent(agent, aid, args);
		LoggerUtil.log("Agent " + aid.getStr() + " started. AID: " + aid.toString(), true);
		if(args == null || args.get("noUIUpdate", "").equals("")) {
			LoggerUtil.logAgent(aid, SocketMessageType.ADD);
		}
	}

	/**
	 * Starts a server-side agent.
	 */
	@Override
	public AID startServerAgent(AgentClass agClass, String runtimeName) {
		String host = NodeManager.getNodeName();

		if (host == null) host = AID.HOST_NAME;
		if (agClass.args != null) {
			host = agClass.args.get("host", AID.HOST_NAME);
		}
		AID aid = new AID(runtimeName, host, agClass);
		startServerAgent(aid, agClass.args, true);
		return aid;
	}

	/**
	 * Starts a client-side agent. Not implemented. It is done at the client-side page, from the /siebog-war/js/radigost/radigost.js file.
	 */
	@Override
	public AID startClientAgent(AgentClass agClass, String name) {
		return null;
	}

	/**
	 * Stops an agent. Removes it from the running agents cache.
	 */
	@Override
	public void stopAgent(AID aid) {
		Agent agent = getCache().get(aid);
		if (agent != null) {
			agent.stop();
			getCache().remove(aid);

			LoggerUtil.log("Stopped agent: " + aid, true);
			LoggerUtil.logAgent(aid, SocketMessageType.REMOVE);
		}
	}
	
	/**
	 * Returns the list of AIDs of all running (created) agents. 
	 */
	@Override
	public List<AID> getRunningAgents() {
		Set<AID> set = getCache().keySet();
		if (set.size() > 0) {
			try {
				AID aid = set.iterator().next();
				try {
					ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), true), Agent.class, SiebogNode.LOCAL);
				} catch (Exception ex) {
					ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), false), Agent.class, SiebogNode.LOCAL);
				}
			} catch (Exception ex) {
				getCache().clear();
				return new ArrayList<AID>();
			}
		}
		return new ArrayList<AID>(set);
	}

	/**
	 * Returns an agent based on the given runtimeName.
	 * 
	 *  @param runtimeName runtime name of an agent.
	 * 
	 */
	@Override
	public AID getAIDByRuntimeName(String runtimeName) {
		// don't throw an exception if not found, because it will be intercepted
		return findInRunning(runtimeName, getRunningAgents());
	}

	@Override
	public void pingAgent(AID aid) {
		try {
			Agent agent = getCache().get(aid);
			agent.ping();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unable to ping the agent.", ex);
		}
	}

	/**
	 * Returns a reference to a running (created) agent from a given AID.
	 * 
	 * @param aid
	 * @return
	 */
	public Agent getAgentReference(AID aid) {
		// don't throw an exception here if there's no such agent
		return getCache().get(aid);
	}

	/**
	 * Returns the agent cache from the Infinispan.
	 * 
	 * @return
	 */
	private Cache<AID, Agent> getCache() {
		if (agents == null)
			agents = GlobalCache.get().getRunningAgents();
		return agents;
	}

	/**
	 * Returns JDNI lookup string for a given agent class. 
	 * 
	 * @param agClass
	 * @param stateful
	 * @return
	 */
	private String getAgentLookup(AgentClass agClass, boolean stateful) {
		if (inEar(agClass)) {
			// in ear file
			if (stateful)
				return String.format("ejb:%s//%s!%s?stateful", agClass.getModule(),
						agClass.getEjbName(), Agent.class.getName());
			else
				return String.format("ejb:%s//%s!%s", agClass.getModule(), agClass.getEjbName(),
						Agent.class.getName());
		} else {
			// in jar file
			if (stateful)
				return String.format("ejb:/%s//%s!%s?stateful", agClass.getModule(),
						agClass.getEjbName(), Agent.class.getName());
			else
				return String.format("ejb:/%s//%s!%s", agClass.getModule(), agClass.getEjbName(),
						Agent.class.getName());
		}
	}

	/**
	 * Returns true if the agent is in the EAR (as opposed to JAR).
	 * @param agClass
	 * @return
	 */
	private boolean inEar(AgentClass agClass) {
		if (agClass.getModule().contains("/"))
			return true;
		return false;
	}

	/**
	 * Initializes an agent.
	 * 
	 * @param agent
	 * @param aid
	 * @param args
	 */
	private void initAgent(Agent agent, AID aid, AgentInitArgs args) {
		// the order of the next two statements matters. if we call init first and the agent
		// sends a message from there, it sometimes happens that the reply arrives before we
		// register the AID. also some agents might wish to terminate themselves inside init.
		getCache().put(aid, agent);
		agent.init(aid, args);
	}

	/**
	 * Returns the AID for the agent given by its runtimeName.
	 * 
	 * @param runtimeName
	 * @param running list of all running (created) agent AIDs.
	 * @return
	 */
	private AID findInRunning(String runtimeName, List<AID> running) {
		for (AID aid : running) {
			if (aid.getName().equals(runtimeName)) {
				return aid;
			}
		}
		return null;
	}
	
	@Override
	public void move(AID aid, String host, List<ObjectField> agent) {
		sendAgent(host, agent);
		stopAgent(aid);
	}
	
	private void sendAgent(String host, List<ObjectField> agent) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://"+host+"/siebog-war/rest/connection");
		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
		rest.moveAgent(agent);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void reconstructAgent(List<ObjectField> agent) {
		Agent localAgent = null;
		for(int i = 0; i < agent.size(); i++) {
			if(agent.get(i).getName().equals("Aid")) {
				LinkedHashMap<String, Object> aidMap = (LinkedHashMap) agent.get(i).getValue();
				LinkedHashMap<String, Object> agClassMap = (LinkedHashMap) aidMap.get("agClass");
				
				AID aid = new AID((String)aidMap.get("name"), NodeManager.getNodeName(), new AgentClass((String)agClassMap.get("module"), (String)agClassMap.get("ejbName"), (String)agClassMap.get("path")));
				localAgent = getAgentReference(startServerAgent(aid.getAgClass(), aid.getName()));
				agent.remove(i);
				break;
			}
		}
		localAgent.reconstruct(agent);
	}

}
