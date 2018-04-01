importScripts("/siebog-war/js/radigost/agent.js");

function Ping() {
	this.messageProcessed = false;
};

Ping.prototype = new Agent();

Ping.prototype.onMessage = function(msg) {
	console.log("Ping received message: " + msg.performative + ", with content: " + msg.content);
	switch (msg.performative) {
	case "REQUEST":
		var pongAid = aid.createAid("pong", this.myAid.host);
		var msgToPong = new ACLMessage("REQUEST", [pongAid]);
		msgToPong.sender = this.myAid;
		msgToPong.content = "Message from ping.";
		this.post(msgToPong);
		break;
	case "INFORM":
		console.log("Pong replied: " + msg.content);
		break;
	}
};
/*
 * This links this agent to its Worker. 
 */
setAgentInstance(new Ping());
