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


/**
 * For simple processors that do not need extra configuration.
 *
 * @author aclement
 *
 */
public class SimpleProcessor<I, O> extends AbstractProcessor<I, O> {

	public SimpleProcessor(String name) {
		super(name);
	}

	@Override
	public SimpleProcessor<I, O> copy() {
		SimpleProcessor<I, O> newModule = new SimpleProcessor<I, O>(this.name);
		newModule.copyOptionsFrom(this);
		return newModule;
	}

}
