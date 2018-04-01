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

package siebog.radigost;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.script.Invocable;
import javax.script.ScriptException;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import radigost.WebClientSocket;
import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.rest.SiebogRest;
import siebog.util.JSON;
import siebog.util.NodeManager;
import util.LoggerUtil;

/**
 * Stub representation of a Radigost agent. Any messages sent to this instance
 * will be forwarded to the client-side agent.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class RadigostStub extends XjafAgent {
	private static final long serialVersionUID = 1L;
	// for speed purposes
	public static final AgentClass AGENT_CLASS = AgentClass.forRadigostSiebogEjb(RadigostStub.class);
	/**
	 * If true, this instance is just a Stub for the client-side agent, so all
	 * messages to this instance end up at the client-side agent. If false, then
	 * this instance holds an JavaScript agent who came from the browser and is
	 * executed within the JavaScript virtual machine on the server.
	 */
	private boolean emptyStub;
	private transient Invocable invocable;
	private transient Object jsAgent;

	// Alternative way of calling webSocket from the siebog-war project.
	// @Inject
	// @Default
	// private Event<ACLMessage> webSocketEvent;

	@EJB
	WebClientSocket webSocketEvent;

	@Override
	protected void onInit(AgentInitArgs args) {
		if (args == null || args.get("url", null) == null) {
			emptyStub = true;
		} else {
			emptyStub = false;
			loadJsAgent(args.get("realPath", ""), args.get("state", "{}"), 
					args.get("pathToAgent", ""), args.get("pathToAid", ""), 
					args.get("pathToRadigostConstants", ""), args.get("pathToAcl", ""));
		}
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (emptyStub) {
			// webSocketEvent.fire(msg);
			webSocketEvent.sendMessageToClient(msg);
			LoggerUtil.logMessage(msg, myAid);
		} else {
			if (msg.performative == Performative.RESUME) {
				try {
					invocable.invokeMethod(jsAgent, "onArrived", NodeManager.getNodeName(), true);
				} catch (NoSuchMethodException | ScriptException ex) {
					throw new IllegalStateException(ex);
				}
			} else {
				sendMsgToJsAgent(msg);
			}
		}
	}

	/**
	 * Loads JavaScript agent whose internal state came inside the ACL message.
	 * The agent must reside in the web root of the siebog-war web project.
	 * 
	 * @param realPath
	 *            - local path of the agent that has just arrived
	 * @param pathToAgent
	 *            - local path of the /siebog-war/js/radigost/agent.js
	 * @param state
	 *            - internal state of the the agent that has just arrived
	 */
	private void loadJsAgent(String realPath, String state, 
			String pathToAgent, String pathToAid, 
			String pathToRadigostConstants, String pathToAcl) {
		try {
			invocable = new ScriptLoader(pathToAgent, pathToAid, 
					pathToRadigostConstants, pathToAcl).load(realPath, state);
			jsAgent = invocable.invokeFunction("getAgentInstance");
			// This code is executed when JavaScript server-side agent posts an ACL message.
			Function<Object, Void> post = (arg) -> {
				if (arg instanceof ScriptObjectMirror) {
					ScriptObjectMirror _arg = (ScriptObjectMirror) arg;
					
					// _arg is a message sent from the JavaScript agent.
					// It is either service message, or ACL message.
					// Service message looks like this:
					/*
						var msg = {
							opcode : "4", //opCode.MOVE_TO_CLIENT,
							myAid : JSON.stringify(this.myAid),
							state : JSON.stringify(agState)
						};
					*/
					Integer opcode = (Integer) _arg.get("opcode"); 
					// MOVE_TO_CLIENT
					if (opcode != null && opcode.intValue() == 4) { // MOVE_TO_CLIENT
						String aidStr = (String) _arg.get("myAid");
						String stateStr = (String) _arg.get("state");
						ACLMessage mess = new ACLMessage();
						mess.opcode = opcode;
						AID receiver = JSON.g.fromJson(aidStr, AID.class);
						mess.userArgs.put("state", stateStr);
						mess.receivers.add(receiver);
						mess.performative = Performative.PROPAGATE;
						webSocketEvent.sendMessageToClient(mess);
						agm().stopAgent(myAid);
					} else {
						// Regular message
						String content = (String) _arg.get("content");
						String performativeStr = (String) _arg.get("performative");
						ScriptObjectMirror receiversObj = (ScriptObjectMirror) _arg.get("receivers");
						List<AID> receivers = getReceivers(receiversObj);
						ACLMessage msg = new ACLMessage(Performative.valueOf(performativeStr));
						msg.content = content;
						msg.receivers = receivers;
						msm().post(msg);
						
					}
				}
				return null;
			};
			Function<String, Void> log = (arg) -> {
				LoggerUtil.log(arg, true);
				return null;
			};
			Function<String, Void> moveToServer = (arg) -> {
				String newHost = arg;
				try {
					String jsState = JSON.g.toJson(invocable.invokeMethod(jsAgent, "getState"));
					ResteasyClient client = new ResteasyClientBuilder().build();
					ResteasyWebTarget rtarget = client.target("http://" + newHost + "/siebog-war/rest/managers");
					SiebogRest rest = rtarget.proxy(SiebogRest.class);
					rest.acceptRadigostAgent(jsState, null);
					agm().stopAgent(myAid);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			};
			// The post, log, and moveToServer arguments will override client-side methods,
			// since the agent is now on the server.
			// Look at the /siebog-war/radigostExamples/mobileAgent.js
			invocable.invokeMethod(jsAgent, "setState", "{\"myAid\":" + JSON.g.toJson(this.myAid) + "}");
			invocable.invokeMethod(jsAgent, "onInit", null, post, log, moveToServer);
			invocable.invokeMethod(jsAgent, "onArrived", NodeManager.getNodeName(), true);
		} catch (ScriptException | NoSuchMethodException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private List<AID> getReceivers(ScriptObjectMirror rcvrs) {
		ScriptObjectMirror[] aids = (ScriptObjectMirror[]) ScriptUtils.convert(rcvrs, ScriptObjectMirror[].class);
		List<AID> ret = new ArrayList<AID>();
		for (ScriptObjectMirror som : aids) {
			AID aid = getAid(som);
			ret.add(aid);
		}
		return ret;
	}

	private AID getAid(ScriptObjectMirror som) {
		String name = (String) som.get("name");
		String host = (String) som.get("host");
		boolean radigost = (Boolean) som.get("radigost");
		AID ret = new AID(name, host, null);
		ret.radigost = radigost;
		return ret;
	}

	/**
	 * Sends an ACL message to the JavaScript agent who came from the client.
	 * 
	 * @param msg
	 */
	private void sendMsgToJsAgent(ACLMessage msg) {
		String jsonMsg = msg.toString();
		try {
			invocable.invokeMethod(jsAgent, "onMessage", jsonMsg);
		} catch (NoSuchMethodException | ScriptException ex) {
			throw new IllegalStateException(ex);
		}
	}
	
}
