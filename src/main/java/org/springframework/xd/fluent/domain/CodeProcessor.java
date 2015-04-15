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

import java.util.Collection;

import org.springframework.xd.fluent.internal.XDRestClient;
import org.springframework.xd.rest.domain.ModuleDefinitionResource;

/**
 * A code processor is a processor driven by compiled code, typically either a Java lambda or a piece of rx java stream
 * processing logic.
 *
 * @author aclement
 *
 */
public class CodeProcessor extends AbstractProcessor {

	private CodeProcessorType type;

	public CodeProcessor(byte[] serializedLambda, CodeProcessorType type) {
		super("code-" + CodeProcessor.nextAvailableId());
		this.type = type;
	}

	public CodeProcessorType getType() {
		return this.type;
	}

	private static int nextAvailableId() {
		Collection<ModuleDefinitionResource> modules = XDRestClient.getInstance().listModules();
		int highestFound = -1;
		for (ModuleDefinitionResource mdr : modules) {
			if (mdr.getName().startsWith("code-")) {
				int number = Integer.parseInt(mdr.getName().substring(5));
				if (number > highestFound) {
					number = highestFound;
				}
			}
		}
		return highestFound++;
	}
}
