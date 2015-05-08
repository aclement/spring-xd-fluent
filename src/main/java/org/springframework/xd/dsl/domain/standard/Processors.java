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

package org.springframework.xd.dsl.domain.standard;

import org.springframework.xd.dsl.domain.Processor;
import org.springframework.xd.dsl.domain.SimpleProcessor;
import org.springframework.xd.tuple.Tuple;


/**
 * Processor factory.
 *
 * @author aclement
 *
 */
public class Processors {

	// TODO required parameters should be on the factory methods for the processors/sinks/sources

	public static Processor<Object, String> transform(String expression) {
		SimpleProcessor<Object, String> processor = new SimpleProcessor<Object, String>("transform");
		processor.setOption("expression", expression);
		return processor;
	}

	public static Processor<Object, String> transform() {
		SimpleProcessor<Object, String> processor = new SimpleProcessor<Object, String>("transform");
		return processor;
	}

	// TODO not sure the generics are right here...
	public static Processor<String, String> filter(String expression) {
		SimpleProcessor<String, String> processor = new SimpleProcessor<String, String>("filter");
		processor.setOption("expression", expression);
		return processor;
	}

	public static Processor<String, Tuple> jsonToTuple() {
		SimpleProcessor<String, Tuple> processor = new SimpleProcessor<>("json-to-tuple");
		return processor;
	}

	/**
	 * To be used when constructing a processor which has no direct reference method in this factory. (e.g.
	 * Processor.custom("myProcessorModule"))
	 */
	public static <I, O> Processor<I, O> custom(String name) {
		return new SimpleProcessor<I, O>(name);
	}

	public static Processor test() {
		SimpleProcessor processor = new SimpleProcessor("test");
		return processor;
	}
}
