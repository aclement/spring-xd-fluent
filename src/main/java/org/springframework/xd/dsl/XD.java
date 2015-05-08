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

import java.util.Properties;

import org.springframework.xd.dsl.domain.SerializableSupplier;
import org.springframework.xd.dsl.domain.Source;
import org.springframework.xd.fluent.internal.XDRestClient;

/**
 * Entry point to the fluent API for creating streams. This contains helper methods that provide a 'concise' entry
 * point, an alternative to using the <tt>new StreamBuilder()</tt> entry point.
 *
 * @author aclement
 *
 */
public class XD {

	/**
	 * Sometimes you may want a stream factory to share some level of configuration, rather than directly using
	 * XD.source as the entry point above.
	 */
	public static StreamBuilder configuredStreamBuilder(Properties configuration) {
		return new StreamBuilder(configuration);
	}

	/**
	 * Begin a new stream with a source.
	 */
	public static <T> PartialStream<T> source(Source<T> source) {
		PartialStream<T> stream = PartialStream.<T> newStream();
		stream.setSource(source);
		return stream;
	}

	/**
	 * Begin a new stream with a source driven via a java.util.function.Supplier
	 */
	public static <T> PartialStream<T> source(SerializableSupplier<T> source) {
		PartialStream<T> streamBuilder = PartialStream.<T> newStream();
		streamBuilder.setSource(source);
		return streamBuilder;
	}


	/**
	 * Delete all 'code' related streams and modules on the target XD instance.
	 */
	public static void cleanup() {
		XDRestClient xdrc = XDRestClient.getInstance();
		xdrc.destroyCodeStreams();
		xdrc.deleteCodeModules();
	}

	public static XDRestClient restApi() {
		XDRestClient xdrc = XDRestClient.getInstance();
		return xdrc;
	}


}
