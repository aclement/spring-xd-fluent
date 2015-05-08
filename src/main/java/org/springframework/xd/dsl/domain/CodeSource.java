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

package org.springframework.xd.dsl.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A code source is a source driven by compiled code, typically either a Java lambda or a piece of RxJava stream
 * processing logic.
 *
 * @author aclement
 *
 */
public class CodeSource extends AbstractSource<Object> implements CodeModule {

	private int id;

	private CodeType type;

	private Set<Resource> resources;

	public String streamName; // set once known

	@Override
	public CodeSource copy() {
		CodeSource newModule = new CodeSource(null, this.type, this.id);
		newModule.copyOptionsFrom(this);
		newModule.resources = copyResources();
		newModule.streamName = streamName; // may not be set yet, that is OK
		return newModule;
	}

	private Set<Resource> copyResources() {
		Set<Resource> newResources = null;
		if (resources != null) {
			newResources = new HashSet<>();
			for (Resource resource : resources) {
				newResources.add(resource);
			}
		}
		return newResources;
	}

	// NOTE: the name used here will be prefixed with the stream name when it gets defined
	public CodeSource(byte[] serializedLambda, CodeType type, int id) {
		super("code-" + Integer.toString(id));
		this.type = type;
		this.id = id;
	}

	@Override
	public CodeType getType() {
		return this.type;
	}

	@Override
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	@Override
	public String getName() {
		return this.streamName + "-" + super.getName();
	}

	public void addResource(Resource resource) {
		if (resources == null) {
			resources = new HashSet<>();
		}
		resources.add(resource);
	}

	@Override
	public Set<Resource> getResourcesToPackage() {
		return Collections.unmodifiableSet(resources);
	}

	@Override
	public Class getInputType() {
		return null;
	}

	@Override
	public Class getOutputType() {
		return Object.class;//TODO placeholder
	}
}
