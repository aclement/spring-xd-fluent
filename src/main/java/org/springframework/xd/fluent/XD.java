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

package org.springframework.xd.fluent;

import org.springframework.xd.fluent.domain.Source;
import org.springframework.xd.fluent.domain.StreamStep;

/**
 * Entry point to the fluent API for creating streams.
 * @author aclement
 *
 */
public class XD {

	public static <T> StreamStep<T> source(Source<T> source) {
		StreamStep<T> stream = StreamStep.<T>newStream();
		stream.setSource(source);
		return stream;
	}

}
