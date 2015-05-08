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

package org.springframework.xd.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.xd.dsl.domain.Processor;
import org.springframework.xd.dsl.domain.Sink;
import org.springframework.xd.dsl.domain.Source;

/**
 * Encapsulates the state of a stream and accumulates information as it is extended with more steps.
 */
class StreamState {

	String name;

	Source<?> source;

	List<Processor<?, ?>> processors;

	Sink<?> sink;

	int stepCount = 1;

	boolean isDeployable = false;

	boolean isCreated = false;

	boolean usesCodeModules = false;

	private Properties configuration;

	public StreamState(Properties configuration) {
		this.configuration = configuration;
	}

	private StreamState() {

	}

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

	public Source getSource() {
		return source;
	}

	public Sink getSink() {
		return sink;
	}

	public List<Processor<?, ?>> getProcessors() {
		return processors;
	}

	/**
	 * When a PartialStream is extended, it gets a new state based on the preceeding state. This enables the head of a
	 * definition to be re-used multiple times with different 'tails'.
	 *
	 * @return
	 */
	public StreamState copy() {
		StreamState s = new StreamState();
		s.configuration = configuration;
		s.name = name;
		s.source = source.copy();
		List<Processor<?, ?>> newProcessors = new ArrayList<>();
		for (Processor p : processors) {
			newProcessors.add(p.copy());
		}
		s.processors = newProcessors;
		s.sink = sink;
		s.stepCount = stepCount;
		s.isDeployable = isDeployable;
		s.isCreated = isCreated;
		s.usesCodeModules = usesCodeModules;
		return s;
	}
}
