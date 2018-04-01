package siebog.rest;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.internal.StringMap;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.AgentManager;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.MessageManager;
import siebog.radigost.RadigostStub;
import siebog.util.JSON;
import siebog.util.NodeManager;

@Stateless
@LocalBean
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Remote(SiebogRest.class)
@Path("/managers")
public class SiebogRestBean implements SiebogRest {

	@EJB
	AgentManager agm;

	@EJB
	MessageManager msm;

	@Override
	public List<AgentClass> getAvailableAgentClasses() {
		return agm.getAvailableAgentClasses();
	}

	@Override
	public AID startServerAgent(AgentClass agClass, String runtimeName) {
		return agm.startServerAgent(agClass, runtimeName);
	}

	@Override
	public List<AID> getRunningAgents() {
		return agm.getRunningAgents();
	}

	@Override
	public void stopAgent(AID aid) {
		agm.stopAgent(aid);
	}

	@Override
	public List<String> getPerformatives() {
		return msm.getPerformatives();
	}

	@Override
	public void post(ACLMessage msg) {
		msm.post(msg, 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String acceptRadigostAgent(String agentState, ServletContext ctx) {
		Map<String, Object> map = JSON.g.fromJson(agentState, Map.class);
		AgentInitArgs args = new AgentInitArgs();
		String url = (String) map.get("url");
		args.put("url", url);
		String realPath = ctx.getRealPath(url);
		realPath = realPath.replaceAll("\\\\" + Agent.SIEBOG_WAR + "\\\\", "\\\\");
		args.put("realPath", realPath);
		args.put("pathToAgent", ctx.getRealPath("/js/radigost/agent.js"));
		args.put("pathToAid", ctx.getRealPath("/js/radigost/aid.js"));
		args.put("pathToRadigostConstants", ctx.getRealPath("/js/radigost/radigost-constants.js"));
		args.put("pathToAcl", ctx.getRealPath("/js/siebog/acl.js"));
		args.put("state", agentState);
		StringMap<Object> myAid = (StringMap<Object>) map.get("myAid");
		AID aid = new AID(myAid.get("name").toString(), NodeManager.getNodeName(), RadigostStub.AGENT_CLASS);
		aid.radigost = true;
		agm.startServerAgent(aid, args, true);
		return aid.getStr();
	}

	@Override
	public String getHostName() {
		return NodeManager.getNodeName();
	}
	
}
