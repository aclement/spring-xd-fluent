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
 * Basic implementation of a Processor.
 *
 * @author aclement
 */
public abstract class AbstractProcessor<I, O> extends AbstractModule<I, O> implements Processor<I, O> {

	public AbstractProcessor(String moduleName) {
		super(moduleName, ModuleType.processor);
	}

	@Override
	public AbstractProcessor<I, O> setOption(String optionName, String optionValue) {
		return (AbstractProcessor<I, O>) super.setOption(optionName, optionValue);
	}
}
