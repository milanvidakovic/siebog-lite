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

package siebog.agents.pingpong;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import util.LoggerUtil;

/**
 * Example of a ping agent.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="nikol.luburic@uns.ac.rs">Nikola Luburic</a>
 */
@Stateful
@Remote(Agent.class)
public class Ping extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private String nodeName;

	@Override
	protected void onInit(AgentInitArgs args) {
		nodeName = getNodeName();
		LoggerUtil.log("Ping created on " + nodeName, true);
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		LoggerUtil.logMessage(msg, myAid);
		if (msg.performative == Performative.REQUEST) { // inital request
			// send a request to the Pong agent, whose name is defined in the message content
			AID pongAid = new AID(msg.content, new AgentClass(Agent.SIEBOG_MODULE, Pong.class.getSimpleName()));
			ACLMessage msgToPong = new ACLMessage(Performative.REQUEST);
			msgToPong.sender = myAid;
			msgToPong.receivers.add(pongAid);
			// use the message manager to publish the request
			msm().post(msgToPong);
		} else if (msg.performative == Performative.INFORM) {
			// wait for the message
			ACLMessage msgFromPong = msg;
			// we can put and retrieve custom user arguments using the userArgs field of the ACL message
			Map<String, Serializable> args = new HashMap<>(msgFromPong.userArgs);
			args.put("pingCreatedOn", nodeName);
			args.put("pingWorkingOn", getNodeName());

			LoggerUtil.log("Ping-Pong interaction details: ", true);
			for (Entry<String, Serializable> e : args.entrySet()) {
				LoggerUtil.log(e.getKey() + " " + e.getValue(), true);
			}
		}
	}
}