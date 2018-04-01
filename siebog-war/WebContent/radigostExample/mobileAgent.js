importScripts("/siebog-war/js/radigost/agent.js");

function MobileAgent() {
	this.num = 42;
};

MobileAgent.prototype = new Agent();

/* If needed to override onInit from Agent.prototype.onInit, then it should be done like this:
MobileAgent.prototype.onInit = function(args, _post, _log, _moveToServer) {
	// Call the parent onInit method (needed only if the agent is migrating to the JavaEE server)
	Agent.prototype.onInit.call(this, args, _post, _log, _moveToServer);
};
*/
/*
 * This function is invoked when this agent arrives on the server (from browser), or on the browser (from server).
 */
MobileAgent.prototype.onArrived = function(host, isServer) {
	this.num++;
	if (isServer) {
		this.isServer = true;
		print("PRINT: I'm at " + host + " server, and the number is " + this.num);
		console.log("CONSOLE.LOG: I'm at " + host + " server, and the number is " + this.num);
	} else {
		this.isServer = false;
		console.log("I just came from " + host + " to my browser, and the number is " + this.num);
	}
}

MobileAgent.prototype.onMessage = function(msg) {
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
			this.moveToServer(msg.content);
		} 
	} else {
		console.log("A'm in a client browser, and I have received the following message: " + JSON.stringify(msg));
		// If this agent receives a "move" string in the ACL message content,
		// it will move to the server.
		if (msg.content === "move") {
			this.moveToServer();
		}
	}
}
/*
 * This links this agent to its Worker. 
 */
setAgentInstance(new MobileAgent());