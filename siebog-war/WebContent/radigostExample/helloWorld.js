importScripts("/siebog-war/js/radigost/agent.js");

function HelloWorld() {
	this.messageProcessed = false;
};

HelloWorld.prototype = new Agent();

HelloWorld.prototype.onMessage = function(msg) {
	console.log("Hello from " + this.myAid.str + ", message performative: " + msg.performative + ", message content: " + msg.content);
	this.messageProcessed = true;
	// This will end up in the onStep observer, because we have added it during the creation of the AgentClass which describes this agent.
	// Look at the /siebog-war/radigostExample/agents.js file. 
	this.onStep("Hello from " + this.myAid.str + "!");
};
/*
 * This links this agent to its Worker. 
 */
setAgentInstance(new HelloWorld());
