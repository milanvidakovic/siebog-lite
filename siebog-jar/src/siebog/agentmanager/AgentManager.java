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

import java.io.Serializable;
import java.util.List;

import javax.ws.rs.PathParam;

import siebog.connectionmanager.ObjectField;

/**
 * Remote interface of the agent manager.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface AgentManager extends Serializable {
	/**
	 * Equivalent to startServerAgent(aid, args, true)
	 */

	public void startServerAgent(AID aid, AgentInitArgs args, boolean replace);

	public AID startServerAgent(AgentClass agClass, @PathParam("name") String runtimeName);

	public AID startClientAgent(AgentClass agClass, String runtimeName);

	public void stopAgent(AID aid);

	public List<AID> getRunningAgents();

	public AID getAIDByRuntimeName(String runtimeName);

	public List<AgentClass> getAvailableAgentClasses();

	public void pingAgent(AID aid);

	public void reconstructAgent(List<ObjectField> agent);

	public void move(AID aid, String host, List<ObjectField> agent);
	
}
