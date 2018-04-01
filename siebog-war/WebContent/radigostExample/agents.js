/*
 * The list of all available agent classes that exist in the server.
 */
var agentClasses = [];
/*
 * Current (selected) agent class for agent to be created using the modal dialog.
 */
var agentClass;
/*
 * The list of all running agents in the server. You can send message only to running agents. 
 */
var runningAgents = [];

/*
 * Radigost instance.
 */
var radigost; 

$(document).ready(function() {

createRadigost(function(rd) {
	radigost = rd;
	// websockets hack so the same /siebog-war/js/websocket.js file can be used for both server-side and this client-side agent examples
	window.appendToConsole = function() {}
	
	$("#sendMessage").click(function() {
		var performative = $("#performative option:selected").text();
		if (performative === "---" ) {
			alert("No performative selected.");
			return;
		}
		var str = $("#receiver option:selected").val();
		var receiver = findAgent(str);
		if (!receiver) {
			receiver = findServerAgent(str);
		}
		if (!receiver) {
			alert("No receiver selected.");
			return;
		}
		var receivers = [receiver];
		var content = $("#content").val();
		
		var acl = new ACLMessage(performative, receivers, content);
		radigost.post(acl);
	});

	$("#createAgent").click(function() {
		var runtimeName = $("#runtimeName").val();
		var createStub = $("#createStub").is(":checked");
		var aid = radigost.start(agentClass, runtimeName, createStub);
		if (!createStub) {
			addAgent(aid);
		} else {
			$("#receiver option[value='" + agent.str + "']").remove();
			$("#sender option[value='" + agent.str + "']").remove();
			$("#replyTo option[value='" + agent.str + "']").remove();
		}
	})
		
	findAgent = function(str) {
		for (var aid in radigost.running) {
			var ag = radigost.running[aid];
			if (ag.myAid.str === str) {
				return ag.myAid;
			}
		}
	}
	
	findServerAgent = function(aidStr) {
		for(var i = 0, n = runningAgents.length; i < n; i++) {
			if(aidStr === runningAgents[i].str) {
				return runningAgents[i];
			}
		}
	};
	
	
	addAgent = function(created, server) {
		var found = false;
		var inCombo = $("option[value='" + created.str + "'");
		if (inCombo.length != 0)
			found = true;
		if (!found) {
			$("#receiver").append(
		            "<option value='" + created.str +  "'>" + created.str + "</option>"
	        );
			$("#sender").append(
		            "<option value='" + created.str +  "'>" + created.str + "</option>"
	        );
			$("#replyTo").append(
		            "<option value='" + created.str +  "'>" + created.str + "</option>"
	        );
		}
	}
	
	addServerAgent = function(created) {
		// agent was created on server
		var found = false;
		for (var i = 0; i < runningAgents.length; i++) {
			var running = runningAgents[i];
			if (running.str === created.str) {
				running.radigost = created.radigost;
				found = true;
				break;
			}
		}
		$("#receiver option[value='" + created.str + "']").remove();
		$("#sender option[value='" + created.str + "']").remove();
		$("#replyTo option[value='" + created.str + "']").remove();
		if (!found) {
			runningAgents.push(created);
			
		}
		$("#receiver").append(
	            "<option value='" + created.str +  "'>[" + created.str + "]</option>"
        );
		$("#sender").append(
	            "<option value='" + created.str +  "'>[" + created.str + "]</option>"
        );
		$("#replyTo").append(
	            "<option value='" + created.str +  "'>[" + created.str + "]</option>"
        );
	}
	
	removeServerAgent = function(agent) {
		var found = false;
		for(var i = 0, n = runningAgents.length; i < n; i++) {
			if(agent.str === runningAgents[i].str) {
				runningAgents.splice(i, 1);
				found = true;
				break;
			}
		}
		if (found) {
			$("#receiver option[value='" + agent.str + "']").remove();
			$("#sender option[value='" + agent.str + "']").remove();
			$("#replyTo option[value='" + agent.str + "']").remove();
		}
	}
	
	addClientAgent = function(created) {
		// agent was created on client
		$("#receiver option[value='" + created.str + "']").remove();
		$("#sender option[value='" + created.str + "']").remove();
		$("#replyTo option[value='" + created.str + "']").remove();

		$("#receiver").append(
	            "<option value='" + created.str +  "'>" + created.str + "</option>"
        );
		$("#sender").append(
	            "<option value='" + created.str +  "'>" + created.str + "</option>"
        );
		$("#replyTo").append(
	            "<option value='" + created.str +  "'>" + created.str + "</option>"
        );
	}
	
	removeClientAgent = function(agent) {
		$("#receiver option[value='" + agent.str + "']").remove();
		$("#sender option[value='" + agent.str + "']").remove();
		$("#replyTo option[value='" + agent.str + "']").remove();
	}
	
	$('#myModal').on('shown.bs.modal', function() {
		$('#runtimeName').focus();
		$('#runtimeName').select();
	});
	
	$("#runtimeName").keypress(function(event) {
		if (event.which == 13) {
			$("#createAgent").click();
		}
	});
	
	$("#agentClasses").on("click", "tr", function() {
		agentClass = agentClasses[this.id];
		$("#myModal").modal();
	});

	// Add client-side agents manually.
	var i = 0;
	var agent = "helloWorld.js";
	// The helloWorld.js agent will have its own observer. This observer is passed as an argument to the
	// radigost.createAgentObserver function. That function will create an observer, which will be 
	// added as a second argument to the AgentClass constructor.
	var observer = radigost.createAgentObserver(function(aid, info) {console.log("OBSERVER, aid: " + aid.str + ", info: " + info);});
	// AgentClass: url, agentObserver, agentInitArgs
	agentClasses.push(new AgentClass(window.location.pathname  + agent, observer, null));
	$("#agentClasses").append(
            "<tr id='" + i++ +  "' class='agentClass' style='cursor:pointer'><td>" + agent + "<td></tr>"
    );
	agent = "ping.js";
	agentClasses.push(new AgentClass(window.location.pathname  + agent, null, null));
	$("#agentClasses").append(
            "<tr id='" + i++ +  "' class='agentClass' style='cursor:pointer'><td>" + agent + "<td></tr>"
    );
	agent = "pong.js";
	agentClasses.push(new AgentClass(window.location.pathname  + agent, null, null));
	$("#agentClasses").append(
            "<tr id='" + i++ +  "' class='agentClass' style='cursor:pointer'><td>" + agent + "<td></tr>"
    );
	agent = "mobileAgent.js";
	agentClasses.push(new AgentClass(window.location.pathname + agent, null, null));
	$("#agentClasses").append(
            "<tr id='" + i++ +  "' class='agentClass' style='cursor:pointer'><td>" + agent + "<td></tr>"
    );
	agent = "testMessages.js";
	agentClasses.push(new AgentClass(window.location.pathname + agent, null, null));
	$("#agentClasses").append(
            "<tr id='" + i++ +  "' class='agentClass' style='cursor:pointer'><td>" + agent + "<td></tr>"
    );
	
	// Get all server-side agents (we can send them message from this page).
	xjaf.getRunningAgents(function(data) {
		runningAgents = data;
		for (var i = 0; i < data.length; i++) {
			var running = data[i];
			running.radigost = false;
			$("#receiver").append(
		            "<option value='" + running.str +  "'>[" + running.str + "]</option>"
            );
			$("#sender").append(
		            "<option value='" + running.str +  "'>[" + running.str + "]</option>"
            );
			$("#replyTo").append(
		            "<option value='" + running.str +  "'>[" + running.str + "]</option>"
            );
		}
	});

	
	for (var performative in aclPerformative) {
		$("#performative").append(
	            "<option value='" + i +  "'>" + performative + "</option>"
        );
	}
  });
});
