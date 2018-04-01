/*
 * Client-side agent class. Holds url to agent, agentobserver object (created by the radigost.createAgentObserver() function) and init args.
 */
function AgentClass(url, agentObserver, agentInitArgs) {
	this.url = url;
	this.agentObserver = agentObserver;
	this.agentInitArgs = agentInitArgs;
}
