/*
 * Agent's AID. 
 * Holds name, host and boolean radigost (true for client-side, false for server-side agents).
 */
var aid = {};
aid.createAid = function(name, host) {
	return {
		name: name,
		host: host,
		radigost: true,
		str: name + "@" + host
	};
}
