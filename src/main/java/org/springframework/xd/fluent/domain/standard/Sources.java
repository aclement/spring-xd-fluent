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

package org.springframework.xd.fluent.domain.standard;

import org.springframework.xd.fluent.domain.Source;

/**
 * Source factory.
 * @author aclement
 *
 */
public class Sources {

	// TODO Generate the factory contents and the source/sink files via annotation processor
	public static Source<String> time() {
		return new TimeSource();
	}

	public static Source<String> feed(String url) {
		FeedSource source = new FeedSource();
		source.setOption("url", url);
		return source;
	}

	public static Source<String> time(String format) {
		TimeSource source = new TimeSource();
		source.setOption("format", format);
		return source;
	}

}
