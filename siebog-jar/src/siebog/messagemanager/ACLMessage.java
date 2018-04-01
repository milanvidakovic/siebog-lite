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

package siebog.messagemanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Default;

import siebog.agentmanager.AID;
import siebog.util.JSON;

/**
 * Represents a FIPA ACL message. Refer to <a
 * href="http://www.fipa.org/specs/fipa00061/SC00061G.pdf">FIPA ACL Message Structure
 * Specification</a> for more details.
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Default
public class ACLMessage implements Serializable {
//	private static final String USERARG_PREFIX = "X-";
	private static final long serialVersionUID = 9089809346282605606L;

	/**
	 *  Denotes the type of the communicative act of the ACL message.
	 */
	public Performative performative;

	/* Participants in Communication */

	/**
	 *  Denotes the identity of the sender of the message.
	 */
	public AID sender;
	/**
	 *  Denotes the identity of the intended recipients of the message.
	 */
	public List<AID> receivers;
	/**
	 *  This parameter indicates that subsequent messages in this conversation
	 *  thread are to be directed to the agent named in the reply-to parameter,
	 *  instead of to the agent named in the sender parameter.
	 */
	public AID replyTo;

	/* Description of Content */

	/**
	 *  Denotes the content of the message; equivalently denotes the
	 *  object of the action.
	 */
	public String content;
	public Serializable contentObj;
	/**
	 * Various arguments.
	 */
	public Map<String, Serializable> userArgs;
	/**
	 *  Denotes the language in which the content parameter is expressed.
	 */
	public String language;
	/**
	 *  Denotes the specific encoding of the content language expression.
	 */
	public String encoding;
	/**
	 *  Denotes the ontology(s) used to give a meaning to the symbols in the content expression.
	 */
	public String ontology;

	/* Control of Conversation */

	/** 
	 * Denotes the interaction protocol that the sending agent is employing with this ACL message.
	 */
	public String protocol;
	/**
	 * Introduces an expression (a conversation identifier) which is used to identify 
	 * the ongoing sequence of communicative acts that together form a conversation.
	 */
	public String conversationId;
	/**
	 *  Introduces an expression that will be used by the responding agent to identify this message.
	 */
	public String replyWith;
	/** 
	 * Denotes an expression that references an earlier action to which this message is a reply.
	 */
	public String inReplyTo;
	/**
	 *  Denotes a time and/or date expression which indicates the latest time by which the sending agent would like to receive a reply.
	 */
	public long replyBy;
	
	/**
	 * Denotes operation code for the radigost. In the direction from the xjaf -> radigost, opcode should be 3 -> MOVE_TO_SERVER
	 */
	public int opcode;

	public ACLMessage() {
		this(Performative.NOT_UNDERSTOOD);
	}

	public ACLMessage(Performative performative) {
		this.performative = performative;
		receivers = new ArrayList<>();
		userArgs = new HashMap<>();
	}

	public ACLMessage(String jsonString) throws Exception {
		System.out.println("ACLMessage JSON: " + jsonString);
		
		ACLMessage m = JSON.g.fromJson(jsonString, ACLMessage.class);
		System.out.println("ACLMessage m: " + m);
		
		this.content = m.content;
		this.conversationId = m.conversationId;
		this.encoding = m.encoding;
		this.inReplyTo = m.inReplyTo;
		this.language = m.language;
		this.ontology = m.ontology;
		this.performative = m.performative;
		this.protocol = m.protocol;
		this.receivers = m.receivers;
		this.replyBy = m.replyBy;
		this.replyTo = m.replyTo;
		this.sender = m.sender;
		this.userArgs = m.userArgs;
	}

	public boolean canReplyTo() {
		return sender != null || replyTo != null;
	}

	public ACLMessage makeReply(Performative performative) {
		if (!canReplyTo())
			throw new IllegalArgumentException("There's no-one to receive the reply.");
		ACLMessage reply = new ACLMessage(performative);
		// receiver
		reply.receivers.add(replyTo != null ? replyTo : sender);
		// description of content
		reply.language = language;
		reply.ontology = ontology;
		reply.encoding = encoding;
		// control of conversation
		reply.protocol = protocol;
		reply.conversationId = conversationId;
		reply.inReplyTo = replyWith;
		return reply;
	}

	@Override
	public String toString() {
		return JSON.g.toJson(this);
	}
}