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

package siebog.connectionmanager;

import java.io.Serializable;

/**
 * Used when agent state is serialized or deserialized during agent move from one server to another.
 * 
 * @author Nikola
 */
@SuppressWarnings("serial")
public class ObjectField implements Serializable{
	private String name;
	private Class<?> type;
	private Object value;
	
	public ObjectField() {
		super();
	}

	public ObjectField(String name, Class<?> type, Object value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
}