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

import org.springframework.xd.dsl.domain.SimpleSink;
import org.springframework.xd.dsl.domain.Sink;

/**
 * Sink factory.
 *
 * @author aclement
 *
 */
public class Sinks {

	public static Sink file() {
		return new FileSink();
	}

	public static Sink<Object> log() {
		return new LogSink();
	}

	public static <I> Sink<I> custom(String name) {
		return new SimpleSink<I>(name);
	}

	public static Sink<Object> richgauge() {
		return new SimpleSink<Object>("rich-gauge");
	}
}
