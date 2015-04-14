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

/**
 * A code processor is a processor driven by compiled code, typically either a Java lambda
 * or a piece of rx java stream processing logic.
 * @author aclement
 *
 */
public class CodeProcessor extends AbstractProcessor {

	// Crude mechanism for naming code processors
	private static int counter = 1;

	private CodeProcessorType type;

	public CodeProcessor(byte[] serializedLambda, CodeProcessorType type) {
		super("code-" + Integer.toString(counter++));
		this.type = type;
	}

	public CodeProcessorType getType() {
		return this.type;
	}

}
