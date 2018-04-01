/*
 * This module holds Siebog REST functions.  
 */
var xjaf = {};
xjaf.post = function(acl) {
	$.ajax({
		url: "/siebog-war/rest/managers/post",
		type:"POST",
		data: JSON.stringify(acl),
		contentType:"application/json",
		dataType:"json",
		complete: function(data) {
			// Message sent
			console.log(data.responseText);
		}
	});
}

xjaf.startServerAgent = function(agentClass, runtimeName) {
	$.ajax({
		url: "/siebog-war/rest/managers/run/" + runtimeName,
		type:"PUT",
		data: JSON.stringify(agentClass),
		contentType:"application/json",
		dataType:"json",
		complete: function(data) {
			// Agent created
			console.log(data.responseText);
		}
	});
}

xjaf.getAvailableAgentClasses = function(callback) {
	$.get('/siebog-war/rest/managers/classes', function(classes) {
		if (classes == undefined || classes == null) {
  		  return;
		}
		callback(classes);
	});
}

xjaf.getRunningAgents = function(callback) {
	$.get('/siebog-war/rest/managers/running', function(data) {
		if (data == undefined || data == null) {
  		  return;
		}
		callback(data);
	});
}

xjaf.getPerformatives = function(callback) {
	$.get('/siebog-war/rest/managers/performatives', function(data) {
		if (data == undefined || data == null) {
  		  return;
		}
		callback(data);
	});
}

xjaf.acceptAgent = function(agent, state) {
	state["url"] = agent.url;
	$.ajax({
		url: "/siebog-war/rest/managers/acceptRadigostAgent",
		type:"PUT",
		data: JSON.stringify(state),
		contentType:"application/json",
		dataType:"json",
		complete: function(data) {
			// Agent sent
			console.log("Agent sent to server: " + data.responseText);
			agent.myAid.radigost = false;
		}
	});
}
