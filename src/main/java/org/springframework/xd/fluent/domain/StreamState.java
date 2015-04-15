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

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the state of a stream and accumulates information as stream steps occur.
 */
public class StreamState {

	String name;

	Source<?> source;

	List<Processor<?, ?>> processors;

	Sink<?> sink;

	boolean isDeployable = false;

	boolean isCreated = false;

	Map<String, List<Resource>> resourcesToPackagePerModule;

	boolean usesCodeModules = false;

	public boolean usesCodeModules() {
		return usesCodeModules;
	}

	public boolean isCreated() {
		return isCreated;
	}

	public boolean isDeployable() {
		return isDeployable;
	}

	public String toDSLString() {
		StringBuilder s = new StringBuilder();
		s.append(source.toDSLString());
		if (processors != null) {
			for (Processor processor : processors) {
				s.append(" | ");
				s.append(processor.toDSLString());
			}
		}
		s.append(" | ");
		s.append(sink.toDSLString());
		return s.toString();
	}

	public List<Processor<?, ?>> getProcessors() {
		return processors;
	}

	public List<Resource> getResourcesToPackageForModule(CodeProcessor processor) {
		return resourcesToPackagePerModule.get(processor.getName());
	}
}
