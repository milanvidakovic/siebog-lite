importScripts("/siebog-war/js/radigost/aid.js", "/siebog-war/js/siebog/acl.js", "/siebog-war/js/radigost/radigost-constants.js");

/*
 * Agent constructor. 
 * myAid field holds the agent's AID.
 */
function Agent() {
	this.myAid = null;
	this.radigostHelper = null;
}

/*
 * Called whenever we need to send an ACL message. 
 */
Agent.prototype.post = function(msg) {
	// This will call the radigost.onWorkerMessage() method.
	self.postMessage(msg);
};

/*
 * Called whenever an agent is initialized.
 * Usually overridden. 
 */
Agent.prototype.onInit = function(args, _post, _log, _moveToServer) {
	// This _post argument is a server-side post which will be used when this agent 
	// moves to the server
	if (_post) {
		this.post = _post;
	}
	if (_log) {
		print = _log;
		console = {};
		console.log = _log;
	}
	if (_moveToServer) {
		this.moveToServer = _moveToServer;
	}
};

/*
 * Called whenever an agent receives a message.
 * Usually overridden. 
 */
Agent.prototype.onMessage = function(msg) {
};

/*
 * Called whenever an agent arrives on the server, or on the client browser.
 * Usually overridden. 
 */
Agent.prototype.onArrived = function(host, isServer) {
};

/*
 * Called within the radigost to activate observer. 
 */
Agent.prototype.onStep = function(step) {
	var msg = {
		opcode : opCode.STEP,
		aid : this.myAid,
		info : step
	};
	this.post(msg);
};

/*
 * This will return agent state as a map. Used when moving agent from browser to the server-side, or vice-versa. 
 */
Agent.prototype.getState = function() {
	var state = {};
	for ( var prop in this)
		if (typeof this[prop] !== "function")
			state[prop] = this[prop];
	return state;
};

/*
 * This will set agent state from a given map. Used when moving agent from the server-side to the browser. 
 */
Agent.prototype.setState = function(state) {
	var st = typeof state === "string" ? JSON.parse(state) : state;
	for ( var prop in st)
		this[prop] = st[prop];
};

/*
 * Initiates agent move to the server.
 */
Agent.prototype.moveToServer = function() {
	var agState = this.getState();
	var msg = {
		opcode : opCode.MOVE_TO_SERVER,
		aid : this.myAid,
		state : agState
	};
	this.post(msg);
};

/*
 * Sends the agent from the server to the browser.
 */
Agent.prototype.moveToClient = function() {
	print("Moving back to client.");
	var agState = this.getState();
	var msg = {
		opcode : opCode.MOVE_TO_CLIENT,
		myAid : JSON.stringify(this.myAid),
		state : JSON.stringify(agState)
	};
	this.post(msg);
};

/** * Web Worker ** */

if (typeof self === "undefined")
	self = new Object(); // needed for the JS scripting engine on the server

/*
 * Holds an instance of an agent inside a Worker.
 */
self.agentInstance = null;

function getAgentInstance() {
	return self.agentInstance;
}

/*
 * This links the agent to its Worker. 
 */
function setAgentInstance(agent) {
	self.agentInstance = agent;
}

/*
 * This method is invoked whenever radigost sends a message to the agent's Worker.
 */
self.onmessage = function(ev) {
	var msg = ev.data;
	if (msg.opcode === opCode.INIT) {
		self.agentInstance.myAid = msg.aid;
		self.agentInstance.onInit(msg.args);
		// send message back to radigost, to invoke observer (if any)
		var initMsg = {
			opcode : opCode.INIT,
			aid : msg.aid
		};
		postMessage(initMsg);
	} else if (msg.opcode === opCode.MOVE_TO_CLIENT) {
		// Agent just arrived from the server (radigost.socket.onmessage).
		// Set the state and call the onArrived.
		self.agentInstance.setState(msg.userArgs.state);
		self.agentInstance.onArrived(self.agentInstance.myAid.host, false);
	} else {
		// Regular ACL message.
		checkPreconditions(msg.interceptor);
		self.agentInstance.onMessage(msg);
		checkPostconditions(msg.interceptor);
	}
};

/*
 * Testing-related function.
 */
function checkPreconditions(interceptor) {
	if (typeof interceptor !== "undefined"
			&& typeof interceptor.preconditions !== "undefined") {
		assertState(interceptor.preconditions, self.agentInstance);
		console.log("Pre-conditions for the agent satisfied.");
	}
}

/*
 * Testing-related function.
 */
function checkPostconditions(interceptor) {
	if (typeof interceptor !== "undefined") {
		if (typeof interceptor.postconditions === "undefined") {
			throw new Error("Interceptors must include post-conditions.");
		}
		assertState(interceptor.postconditions, self.agentInstance);
		console.log("Post-conditions for the agent satisfied.");
	}
}

/*
 * Testing-related function.
 */
function assertState(expected, actual) {
	for ( var key in expected) {
		if (!actual.hasOwnProperty(key)) {
			throw new Error("Property " + key + " not found.");
		}
		if (expected[key] !== actual[key]) {
			var msg = "Mismatched property " + key + ", expected:"
					+ expected[key] + ", actual:" + actual[key] + "."
			throw new Error(msg);
		}
	}
}
