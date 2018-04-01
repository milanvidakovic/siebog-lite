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

package siebog.radigost;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class is used to load JavaScript from the web root of the siebog-war into the JavaScript 
 * virtual machine on the server-side.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ScriptLoader {
	private String radigostAgentParent;
	private String aidSrc;
	private String radigostConstantsSrc;
	private String aclSrc;

	public ScriptLoader(String pathToAgent, String pathToAid, 
			String pathToRadigostConstants, String pathToAcl) {
		// "/siebog-war/js/radigost/agent.js"
		radigostAgentParent = getJSSource(pathToAgent);
		aidSrc = getJSSource(pathToAid);
		radigostConstantsSrc = getJSSource(pathToRadigostConstants);
		aclSrc = getJSSource(pathToAcl);
	}

	/**
	 * Loads an agent from the realPath in the server file system. 
	 * 
	 * @param realPath - local path within the server file system which points to the agent's source.
	 * @param state - agent's state to be reconstructed when loading is finished.
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	public Invocable load(String realPath, String state) throws ScriptException, NoSuchMethodException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		// Load and execute agent's source.
		engine.eval(getFullAgentSouce(realPath));
		Invocable invocable = (Invocable) engine;
		// Obtain agent's instance.
		Object jsAgent = invocable.invokeFunction("getAgentInstance");
		// inject state and signal arrival
		invocable.invokeMethod(jsAgent, "setState", state);
		
		return invocable;
	}

	/**
	 * Reads the complete source from the given path.
	 * 
	 * @param name path to the JavaScript file to be read.
	 * @return source of the given file
	 */
	protected String getJSSource(String name) {
		try {
			InputStream is = new FileInputStream(name);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			return readJSSource(in);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private String readJSSource(BufferedReader in) throws IOException {
		StringBuilder str = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.isEmpty() && !line.startsWith("importScripts"))
				str.append(line).append("\n");
		}
		return str.toString();
	}

	/**
	 * Loads agent's source from the realPath path within the file system.
	 * It concatenates the /siebog-war/js/radigost/agent.js source with the agent's source,
	 * since an agent extends the Agent class from the agent.js file.
	 * @param realPath
	 * @return
	 */
	private String getFullAgentSouce(String realPath) {
		String js = getJSSource(realPath);
		StringBuilder sb = new StringBuilder(radigostAgentParent);
		sb.append(aidSrc);
		sb.append(radigostConstantsSrc);
		sb.append(aclSrc);
		sb.append("\nload(\"nashorn:mozilla_compat.js\");\n");
		sb.append(js);
		return sb.toString();
	}
}
