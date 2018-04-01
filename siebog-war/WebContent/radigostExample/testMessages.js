importScripts("/siebog-war/js/radigost/agent.js");

function MessageAgent() {
	this.num = 42;
};

MessageAgent.prototype = new Agent();

/*
 * This function is invoked when this agent arrives on the server (from browser), or on the browser (from server).
 */
MessageAgent.prototype.onArrived = function(host, isServer) {
	this.num++;
	if (isServer) {
		this.isServer = true;
		print("I'm at " + host + " server, and the number is " + this.num);
	} else {
		this.isServer = false;
		console.log("I just came from " + host + " to my browser, and the number is " + this.num);
	}
}

MessageAgent.prototype.onMessage = function(msg) {
	if (this.isServer) {
		print("I'm on the server and I received the following message:\n" + msg);
		msg = JSON.parse(msg);
		print("Message content: " + msg.content);
		// If this agent receives a "move" string in the ACL message content,
		// it will go back to the browser.
		if (msg.content === "move") {
			print("Got the message to move back to the client.");
			// This will initiate the move.
			this.moveToClient();
		} else {
			// if the content is NOT 'move', then we will read the content as AID
			// and send the message to that AID.
			// For example, server-based agent:
			// {"name":"test","host":"localhost","str":"test@localhost","radigost":false}
			// {"name":"test","host":"maja","str":"test@maja","radigost":false}
			// or, browser-based agent:
			// {"name":"h","host":"maja","str":"h@maja","radigost":true}
			var aidFromContent = JSON.parse(msg.content);
			var msg = new ACLMessage("INFORM", [aidFromContent]);
			msg.sender = this.myAid;
			msg.content = "From server-side JavaScript.";
			this.post(msg);
		}
	} else {
		console.log("A'm in a client browser, and I have received the following message: " + JSON.stringify(msg));
		// If this agent receives a "move" string in the ACL message content,
		// it will move to the server.
		if (msg.content === "move") {
			this.moveToServer();
		} else {
			// if the content is NOT 'move', then we will read the content as AID
			// and send the message to that AID.
			// {"name":"test","host":"localhost","str":"test@localhost","radigost":false}
			var aidFromContent = JSON.parse(msg.content);
			var msg = new ACLMessage("INFORM", [aidFromContent]);
			msg.sender = this.myAid;
			msg.content = "From client-side JavaScript.";
			this.post(msg);
		}
	}
}
/*
 * This links this agent to its Worker. 
 */
setAgentInstance(new MessageAgent());