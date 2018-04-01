/*
 * Creates a radigost instance. 
 * Parameter callback is the callback function which will 
 * called when radigost is ready. That callback has one parameter - radigost instance. 
 */
function createRadigost(callback) {
	$.get('/siebog-war/rest/managers/hostname', function(host) {
		if (host == undefined || host == null) {
  		  return;
		}
		// Variable host is the computer name or host address. 
		// It is obtained from the System.getProperty("jboss.node.name");
		/*
		 * Main radigost object. Holds all methods which are invoked from the client-side html page.
		 */
		var radigost = {
			/*
			 * Computer name or host address.
			 * It must be the same value as the System.getProperty("jboss.node.name");
			 */
			host: host,
			/*
			 * List of running agents (aka created agents).
			 */
			running: {},
			/*
			 * WebSocket for communication with JBoss 
			 */
			socket: new WebSocket("ws://" + window.location.host + "/siebog-war/webclient"),
			/*
			 * Event handler for all messages coming from agents as Workers. 
			 */
			onWorkerMessage: onWorkerMessage,
			/*
			 * Starts one agent.
			 */
			start: start,
			/*
			 * Stops one agent.
			 */
			stop: stop,
			/*
			 * Sends ACL message to the server-side agent.
			 */
			postToServer: postToServer,
			/*
			 * Sends ACL message to the client-side agent.
			 */
			postToClient: postToClient,
			post: post,
			/*
			 * Returns an agent instance using the given AID.
			 */
			getAgent: getAgent,
			/*
			 * Stores an agent instance using the given AID in the repository of created (running) agents.
			 */
			putAgent: putAgent,
			/*
			 * Creates an observer. 
			 */
			createAgentObserver: createAgentObserver
		};

		radigost.socket.onmessage = function(message) {
			var msg = JSON.parse(message.data);
			
			if (msg.opcode === opCode.MOVE_TO_CLIENT) {
				// This opcode means that this message came from the server
				// wanting to move the agent from server to browser
				var aid = msg.receivers[0];
				var state = msg.userArgs["state"];
				state = typeof state === "string" ? JSON.parse(state) : state;
				var ag = radigost.getAgent(aid);
				if (!ag) {
					// agent is not running. Let's try to start one. 
					radigost.start(new AgentClass(state.url, null, null), aid.name, false);
					ag = radigost.getAgent(aid);
				}
				ag.myAid.radigost = true;
				ag.myAid.agClass = undefined;
				// we will send the Worker message to the agent's worker,
				// so it will invoke the onArrived method.
				ag.worker.postMessage(msg);
				removeServerAgent(ag.myAid);
				addClientAgent(ag.myAid);
				return;
			}
			// All other messages came from the server to the client-side agents.
			if (typeof msg.sender === "string") {
				msg.sender = JSON.parse(msg.sender);
			}
			if (typeof msg.replyTo === "string") {
				msg.replyTo = JSON.parse(msg.replyTo);
			}
			for (var i = 0, len = msg.receivers.length; i < len; i++) {
				if (typeof msg.receivers[i] === "string") {
					msg.receivers[i] = JSON.parse(msg.receivers[i]);
				}
			}
			// Send the message to the client-side agent.
			postToClient(msg);
		}

		radigost.socket.onopen = function() {
			// Register this page to the server, so the messages from it 
			// can start coming.
			radigost.socket.send(webClientOpCode.REGISTER + radigost.host);
		}

		radigost.socket.onclose = function(e) {
			console.log("Radigost WebSocket connection closed.");
		}

		radigost.socket.onerror = function(e) {
			console.log("Radigost WebSocket connection error: " + e.data);
		}

		return callback(radigost);
	});
	
}

/*
 * This function is called whenever agent's Worker sends a Worker message to the radigost.
 * It will distribute the message to the recipient, if a regular ACL message has arrived,
 * or it will invoke the corresponding observer, if exists.
 */
function onWorkerMessage(ev) {
	var msg = ev.data;
	if (typeof msg.opcode === "undefined") // a regular message
		post(msg);
	else {
		switch (msg.opcode) {
		case opCode.INIT:
			var ag = radigost.getAgent(msg.aid);
			if (ag !== null && ag.observer !== null)
				ag.observer.onStart(msg.aid);
			break;
		case opCode.STEP:
			var ag = radigost.getAgent(msg.aid);
			if (ag !== null && ag.observer !== null)
				ag.observer.onStep(msg.aid, msg.info);
			break;
		case opCode.MOVE_TO_SERVER:
			var ag = radigost.getAgent(msg.aid);
			if (ag !== null && ag.url !== null)
				// This will actually send a message to the server
				// with the serialized agent state
				// That serialized agent will be restored on the server-side
				// And onArrived function will be called upon arrival.
				xjaf.acceptAgent(ag, msg.state);
			break;
		default:
			throw new Error("Unrecognized OpCode: " + JSON.stringify(msg));
		}
	}
};

/*
 * Starts one agent.
 * AgentClass holds agent name, module name and an observer.
 */
function start(agentClass, name, createStub) {
	// agentClass: url, agentObserver, agentInitArgs
	var newAid = aid.createAid(name, radigost.host);
	var existing = radigost.running[newAid.str];
	if (existing) {
		radigost.stop(newAid);
	}
	var agent = {};
	agent.myAid = newAid;
	agent.url = agentClass.url;
	agent.observer = agentClass.agentObserver;
	
	agent.worker = new Worker(agentClass.url);
	agent.worker.onmessage = onWorkerMessage;
	
	// initialize it
	var msg = {
		opcode: opCode.INIT,
		aid: newAid,
		args: agentClass.agentInitArgs
	};
	agent.worker.postMessage(msg);
	if (createStub) {
		newAid.radigost = false;
		// create the server-side stub
		var agClass = {
                ejbName: "RadigostStub",
                module: "siebog-ear/siebog-jar",
                path: "/siebog-ear/siebog-jar/agents/xjaf"
        };
		xjaf.startServerAgent(agClass, name);
	}
	radigost.putAgent(newAid, agent);
	return newAid;
};

/*
 * Stops agent and removes it from all structures.
 */
function stop(aid) {
	var agent = radigost.running[aid.str];
	if (agent) {
		agent.worker.terminate();
		delete radigost.running[aid.str];
	}
	removeClientAgent(aid);
}

/*
 * Sends a message to the server-side agent.
 */
function postToServer(message) {
	xjaf.post(message);
};

/*
 * Sends a message to the client-side agent's Worker. It will eventually call the onMessage method within agent.
 */
function postToClient(message) {
	for (var i = 0, len = message.receivers.length; i < len; i++) {
		var agent = radigost.getAgent(message.receivers[i]);
		if (agent != null && agent.worker != null) {
			agent.worker.postMessage(message);
		}
	}
};

/*
 *  Sends a message to an agent.
 *  Uses the 'radigost' field of each receiver AID to determine if the agent
 *  is on the client (true) or on the server (false) side.
 */
function post(message) {
	var server = [];
	for (var i = 0, len = message.receivers.length; i < len; i++) {
		var aid = message.receivers[i];
		if (aid.radigost && !aid.agClass) {
			var agent = radigost.getAgent(aid);
			if (agent !== null && agent.worker !== null)
				agent.worker.postMessage(message);
		} else {
			server.push(aid);
		}
	}
	// send to server?
	if (server.length > 0) {
		message.receivers = server;
		xjaf.post(message);
	}
};

/*
 * Returns an agent instance using the given AID.
 */
function getAgent(aid) {
	if (radigost.running.hasOwnProperty(aid.str))
		return radigost.running[aid.str];
	return null;
};

/*
 * Stores an agent instance using the given AID in the repository of created (running) agents.
 */
function putAgent(aid, agent) {
	radigost.running[aid.str] = agent;
};


/*
 * Creates an observer. Client-side JavaScript code can call this function with 
 * callback functions that will be called on INIT and STEP messages.
 */
function createAgentObserver(onStep, onStart, onStop) {
	var agentObserver = {
		onStep: onStep,
		onStart: onStart,
		onStop: onStop
	};
	if(!onStart) {
		agentObserver.onStart = function(aid) {};
	}
	if(!onStop) {
		agentObserver.onStop = function(aid) {};
	}

	return agentObserver;
};