importScripts("/siebog-war/js/radigost/agent.js");

function Pong() {
	this.messageProcessed = false;
};

Pong.prototype = new Agent();

Pong.prototype.onMessage = function(msg) {
	console.log("Pong received message: " + msg.performative + ", with content: " + msg.content);
	switch (msg.performative) {
	case "REQUEST":
		var pingAid = msg.sender;
		var msgToPing = new ACLMessage("INFORM", [pingAid]);
		msgToPing.sender = this.myAid;
		msgToPing.content = "Reply from Pong.";
		this.post(msgToPing);
		break;
	}
};
/*
 * This links this agent to its Worker. 
 */
setAgentInstance(new Pong());
