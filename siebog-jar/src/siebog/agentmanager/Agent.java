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

import siebog.connectionmanager.ObjectField;
import siebog.messagemanager.ACLMessage;

/**
 * Remote interface for agents. All of its methods are for internal purposes only, and should not be
 * called or redefined.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface Agent extends Serializable {
	String SIEBOG_MODULE = "siebog-jar";
	String SIEBOG_EAR = "siebog-ear";
	String SIEBOG_WAR = "siebog-war";

	void init(AID aid, AgentInitArgs args);

	void stop();

	void handleMessage(ACLMessage msg);

	String ping();
	
	AID getAid();
	
	void move(String host);
	
	void reconstruct(List<ObjectField> agent);
	
	List<ObjectField> deconstruct();
}
