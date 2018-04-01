/*
 * This module holds web socket communication code. Whenever some web socket message comes from the Siebog server, it will end up here.
 */
var socket = new WebSocket("ws://" + window.location.host + "/siebog-war/console");

socket.onmessage = function(message) {
	var msg = JSON.parse(message.data);
	if(msg.type === "LOG") {
		var d = new Date();
	    var time = ("0" + d.getHours()).substr(-2) + ':' + ("0" + d.getMinutes()).substr(-2) + ':' + ("0" + d.getSeconds()).substr(-2);
	    appendToConsole(time + " - " + msg.data + "\n");
	} else if(msg.type === "ADD") {
		// A new agent has been created. Add it to the client GUI.
		var agent = msg.data;
		addServerAgent(agent);
	} else if(msg.type === "REMOVE") {
		// An agent has been stopped. Remove it to from the client GUI.
		var agent = msg.data;
		removeServerAgent(agent);
	}
}

socket.onopen = function() {
	console.log("WebSocket for console connection opened.");
}

socket.onclose = function(e) {
	console.log("WebSocket for console connection closed.");
};

socket.onerror = function(e) {
    console.log("WebSocket for console  connection error: " + e.data);
};
