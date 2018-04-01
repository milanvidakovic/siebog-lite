/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package siebog.client;

import java.util.Properties;

import org.jboss.ejb.client.legacy.JBossEJBProperties;

import siebog.util.NodeManager;

/**
 * Helper class for client initialization.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SiebogClient {
	private static final String USERNAME = "guest";
	private static final String PASSWORD = "guest.guest.1";
	private static boolean connected;
	
	/**
	 * Connect to a running Siebog cluster. This method should be called before any other
	 * interaction with the server.
	 * 
	 * @param masterAddress Address of the master node.
	 * @param slaves Address of at least one slave node (if any).
	 */
	public static void connectToCluster(String masterAddress, String... slaves) {
		if (connected)
			return;
		Properties p = new Properties();
		p.put("endpoint.name", "client-endpoint");
		p.put("deployment.node.selector", NodeManager.getNodeName());

		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		p.put("remote.clusters", "ejb");
		p.put("remote.cluster.ejb.username", USERNAME);
		p.put("remote.cluster.ejb.password", PASSWORD);
		// p.put("remote.cluster.ejb.clusternode.selector", RRClusterNodeSelector.class.getName());

		addConnection(p, "master", masterAddress, 8080);
		String connections = "";
		for (String addr : slaves) {
			String name = "s" + addr.replace('.', '_');
			addConnection(p, name, addr, 8080);
			connections += "," + name;
		}
		p.put("remote.connections", "master" + connections);

		
		JBossEJBProperties ejbProps = JBossEJBProperties.fromProperties("", p);
        JBossEJBProperties.getContextManager().setGlobalDefault(ejbProps); 
		connected = true;
	}

	/*
	remote.connections=default
	endpoint.name=client-endpoint
	remote.connection.default.host=maja
	remote.connection.default.port=8080
	remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED=false
	remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS=false
	*/
	
	public static void connect(SiebogNode node) {
		Properties p = new Properties();
		p.put("remote.connections", "default");
		p.put("endpoint.name", "client-endpoint");
		p.put("remote.connection.default.host", node.host);
		p.put("remote.connection.default.port", node.port);
		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", false);
		p.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", false);
		
		JBossEJBProperties ejbProps = JBossEJBProperties.fromProperties("fileName", p);
        JBossEJBProperties.getContextManager().setGlobalDefault(ejbProps); 
	}
	
	private static void addConnection(Properties p, String name, String address, int port) {
		final String prefix = "remote.connection." + name;
		p.put(prefix + ".host", address);
		p.put(prefix + ".port", port + "");
		p.put(prefix + ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
		p.put(prefix + ".username", USERNAME);
		p.put(prefix + ".password", PASSWORD);
	}
}
