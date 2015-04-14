/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.fluent.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic module implementation.
 * 
 * @author aclement
 */
public abstract class AbstractModule<I, O> implements Module<I, O> {

	private String name;

	private Map<String, String> options;

	public AbstractModule(String moduleName) {
		this.name = moduleName;
	}

	public void setOption(String optionName, String optionValue) {
		if (options == null) {
			options = new HashMap<String, String>();
		}
		options.put(optionName, optionValue);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toDSLString() {
		StringBuilder s = new StringBuilder();
		s.append(name);
		if (options != null) {
			for (Map.Entry<String, String> entry : options.entrySet()) {
				s.append(" --").append(entry.getKey()).append('=').append(entry.getValue());
			}
		}
		return s.toString();
	}
}
