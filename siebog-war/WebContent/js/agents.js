/*
 * The list of all available agent classes that exist in the server.
 */
var agentClasses;
/*
 * Current (selected) agent class for agent to be created using the modal dialog.
 */
var agentClass;
/*
 * The list of all running agents in the server. You can send message only to running agents. 
 */
var runningAgents;

$(document).ready(function() {

	$("#clearConsoleLog").click(function() {
		$("#consoleLog").val("");
	});

	$("#sendMessage").click(function() {
		var performative = $("#performative option:selected").text();
		if (performative === "---" ) {
			alert("No performative selected.");
			return;
		}
		var str = $("#receiver option:selected").val();
		var receiver = findAgent(str);
		if (!receiver) {
			alert("No receiver selected.");
			return;
		}
		var receivers = [receiver];
		var content = $("#content").val();
		
		var acl = new ACLMessage(performative, receivers, content);
		xjaf.post(acl);
	});
	
	$("#createAgent").click(function() {
		var runtimeName = $("#runtimeName").val();
		xjaf.startServerAgent(agentClass, runtimeName)
	})

	findAgent = function(str) {
		for(var i = 0; i < runningAgents.length; i++) {
			var agent = runningAgents[i];
			if (agent.str === str) {
				return agent;
			}
		}
	}
	
	addServerAgent = function(created) {
		var found = false;
		for (var i = 0; i < runningAgents.length; i++) {
			var running = runningAgents[i];
			if (running.str === created.str) {
				found = true;
				break;
			}
		}
		if (!found) {
			runningAgents.push(created);
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
	}
	
	removeServerAgent = function(agent) {
		for(var i = 0, n = runningAgents.length; i < n; i++) {
			if(agent.str === runningAgents[i].str) {
				runningAgents.splice(i, 1);
				$("#receiver option[value='" + agent.str + "']").remove();
				$("#sender option[value='" + agent.str + "']").remove();
				$("#replyTo option[value='" + agent.str + "']").remove();
				break;
			}
		}
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
	
	appendToConsole = function(text) {
		var aconsole = $('#consoleLog');
		var oldText = aconsole.val();
		aconsole.val(oldText + text);
	    if(aconsole.length)
	       aconsole.scrollTop(aconsole[0].scrollHeight - aconsole.height());
	}
	
	
	xjaf.getAvailableAgentClasses(function(classes) {
		agentClasses = classes;
		for (var i = 0; i < classes.length; i++) {
			var agClass = classes[i];
			$("#agentClasses").append(
		            "<tr id='" + i +  "' class='agentClass' style='cursor:pointer'><td>" + agClass.module + "$"  + agClass.ejbName + "<td></tr>"
            );
		}
	});
	
	xjaf.getRunningAgents(function(data) {
		runningAgents = data;
		for (var i = 0; i < data.length; i++) {
			var running = data[i];
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
	
	xjaf.getPerformatives(function(data) {
		for (var i = 0; i < data.length; i++) {
			var performative = data[i];
			$("#performative").append(
		            "<option value='" + i +  "'>" + performative + "</option>"
            );
		}
	});
});