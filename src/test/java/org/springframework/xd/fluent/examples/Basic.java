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

package org.springframework.xd.fluent.examples;

import org.springframework.xd.fluent.XD;
import org.springframework.xd.fluent.domain.standard.Sinks;
import org.springframework.xd.fluent.domain.standard.Sources;
import org.springframework.xd.fluent.internal.XDRestClient;

public class Basic {

	public static void main(String[] args) {
		XD.cleanup();
		XD.source(Sources.time()).sink(Sinks.log()).deploy("abc");
		XD.source(Sources.time()).sink(Sinks.log()).deploy("def");
		XDRestClient.getInstance().listStreams();
	}
	//
	//	public static void simpleStream() {
	//		DeployableStream s = XD.source(Sources.time()).sink(Sinks.log());
	//		s.deploy();
	//	}
	//
	//	public static void sourceWithParameter() {
	//		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).sink(Sinks.log());
	//		s.deploy();
	//	}
	//
	//	public static void configuredTransformProcessor() {
	//		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).process(Processors.transform("payload.substring(6)")).sink(
	//				Sinks.log());
	//		s.deploy();
	//	}
	//
	//	public static void lambdaProcessor() {
	//		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).process(payload -> payload.substring(3)).sink(
	//				Sinks.log());
	//		s.deploy();
	//	}
	//
	//
	//	// Needs the 'feed' source from spring-xd-samples (rss-feed-source)
	//	public static void jsonPathFilterAndTransformer() {
	//		DeployableStream s = XD.source(Sources.feed("http://feeds.bbci.co.uk/news/rss.xml")).process(
	//				Processors.filter("#jsonPath(payload,'$.uri').contains('sport')")).process(
	//				Processors.transform("#jsonPath(payload,'$.title')")).sink(
	//				Sinks.log());
	//		s.deploy();
	//	}
	//
	//	public static void doubleLambdaProcessors() {
	//		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).process(payload -> payload.replaceAll(":", ">")).process(
	//				payload -> payload.substring(2)).sink(
	//				Sinks.log());
	//		s.deploy();
	//	}
	//
	//	public static void methodReference() {
	//		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).process(Basic::colonStripper).sink(
	//				Sinks.log());
	//		s.deploy();
	//	}
	//
	//	private static Object colonStripper(Object o) {
	//		return ((String) o).replaceAll(":", "");
	//	}
	//
	//	// replace title:XXX with foo:XXX
	//	public static void rxStreaming() {
	//		DeployableStream s = XD.source(Sources.feed("http://feeds.bbci.co.uk/news/rss.xml")).
	//				process(Processors.jsonToTuple()).
	//				processrx(inputStream ->
	//						inputStream.map(tuple -> {
	//							return tuple.getValue("title").toString();
	//						}).map(data -> tuple().of("foo", data)))
	//				.sink(Sinks.log());
	//		s.deploy();
	//	}
	//
	//	public static void moreComplicatedRx() {
	//		DeployableStream s = XD.source(Sources.feed("http://feeds.bbci.co.uk/news/rss.xml")).
	//				process(Processors.filter("payload.toString().contains('sport')")).
	//				process(Processors.jsonToTuple()).
	//				processrx(inputStream ->
	//						inputStream.map(tuple -> {
	//							return tuple.getValue("title").toString();
	//						}).buffer(5).
	//								map(data -> tuple().of("new_news", mashup(data))))
	//				.process(Processors.transform("payload.toString().substring(82)")).sink(Sinks.log());
	//		s.deploy();
	//	}
	//
	//	// Mashup a few headlines into a new headline
	//	public static String mashup(List<String> ls) {
	//		List<String[]> arrays = new ArrayList<String[]>();
	//		for (int i = 0; i < ls.size(); i++) {
	//			arrays.add(ls.get(i).split(" "));
	//		}
	//		StringBuilder story = new StringBuilder();
	//		Random r = new Random(1);
	//		for (int i = 0; i < 10; i++) {
	//			String[] array = arrays.get(i % ls.size());
	//			story.append(array[(Math.abs(r.nextInt()) % array.length)]);
	//			story.append(" ");
	//		}
	//		return story.toString();
	//	}

}
