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

package org.springframework.xd.dsl.examples;

import static org.springframework.xd.dsl.domain.standard.Processors.transform;
import static org.springframework.xd.dsl.domain.standard.Sinks.log;
import static org.springframework.xd.dsl.domain.standard.Sources.time;
import static org.springframework.xd.tuple.TupleBuilder.tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.xd.dsl.DeployableStream;
import org.springframework.xd.dsl.StreamBuilder;
import org.springframework.xd.dsl.XD;
import org.springframework.xd.dsl.XDUtils;
import org.springframework.xd.dsl.domain.Processor;
import org.springframework.xd.dsl.domain.Source;
import org.springframework.xd.dsl.domain.standard.Processors;
import org.springframework.xd.dsl.domain.standard.Sinks;
import org.springframework.xd.dsl.domain.standard.Sources;

/**
 * Includes examples of everything.
 *
 * @author aclement
 */
public class Examples {

	public static void main(String[] args) {
		wordsmith();
	}

	/**
	 * Build a simple source/sink stream passing the time (once per second) to the sink.
	 */
	public static void simpleTimeLog() {
		XD.source(Sources.time()).sink(Sinks.log()).deploy("timestream", true);
	}

	/**
	 * Build a simple source/sink stream passing the time (once per second) to the sink but deploy it later.
	 */
	public static void simpleStreamDelayedDeployment() {
		DeployableStream ds = XD.source(Sources.time()).sink(Sinks.log());
		ds.deploy("timestream", true);
	}

	/**
	 * As an alternative to the XD shorthand, use the StreamBuilder for construction.
	 */
	public static void simpleStreamUsingStreamBuilder() {
		StreamBuilder sb = new StreamBuilder();
		DeployableStream ds = sb.source(Sources.time()).sink(Sinks.log());
		ds.deploy("timestream", true);
	}

	/**
	 * Build a simple stream with a transform processor in it. Transform processors take a SpEL expression.
	 */
	public static void processors() {
		DeployableStream ds = XD.source(Sources.time()).process(Processors.transform("payload.substring(3)")).sink(
				Sinks.log());
		ds.deploy("timestripper");
	}

	/**
	 * Build a simple stream, using static imports for the time/transform/log elements
	 */
	public static void processorsWithStaticImports2() {
		DeployableStream ds = XD.source(time()).process(transform("payload.substring(3)")).sink(log());
		ds.deploy("timestripper");
	}

	/**
	 * Simple stream where the source is passed a parameter, in this case time() is given a format.
	 */
	public static void parameterizedSources() {
		DeployableStream ds = XD.source(time("HH:mm:ss")).process(transform("payload.substring(3)")).sink(log());
		ds.deploy("timestripper");
	}

	/**
	 * Just pulling out the time configured source and doing that config up front. TODO what is the output type of
	 * time(), should it be Date?
	 */
	public static void parameterizedSources2() {
		// TODO what is the right output type of time()
		Source<String> configuredTimeSource = time().setOption("format", "HH:mm:ss");
		// TODO what is the right in/out types here?
		Processor<Object, String> configuredTransformProcessor = transform().setOption("expression",
				"payload.substring(3)");
		DeployableStream ds = XD.source(configuredTimeSource).process(configuredTransformProcessor).sink(log());
		ds.deploy("timestripper", true);
	}

	/**
	 * Use a Java8 lambda to implement a processor
	 */
	public static void lambdaProcessor() {
		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).process(payload -> payload.substring(3)).sink(
				Sinks.log());
		s.deploy("timestripper", true);
	}


	public static void lambdaSource() {
		DeployableStream ds = XD.source(() -> {
			// Produce a 0-99 random number every 500ms
				XDUtils.sleep(500);
				return Math.abs(new java.util.Random().nextInt() % 100);
			}).sink(Sinks.log());
		ds.deploy("foo");
	}

	public static void lambdaSourceMethodReference() {
		DeployableStream ds = XD.source(Examples::randomNumberGenerator).sink(Sinks.log());
		ds.deploy("foo");
	}

	// Needs the 'feed' source from spring-xd-samples (rss-feed-source)
	public static void jsonPathFilterAndTransformer() {
		DeployableStream s = XD.source(Sources.feed("http://feeds.bbci.co.uk/news/rss.xml")).process(
				Processors.filter("#jsonPath(payload,'$.uri').contains('sport')")).process(
				Processors.transform("#jsonPath(payload,'$.title')")).sink(
				Sinks.log());
		s.deploy("foo", true);
	}


	public static void methodReference() {
		DeployableStream s = XD.source(Sources.time("HH:mm:ss")).process(Examples::colonStripper).sink(
				Sinks.log());
		s.deploy("foo", true);
	}

	private static Object colonStripper(Object o) {
		return ((String) o).replaceAll(":", "");
	}

	// replace title:XXX with foo:XXX
	public static void streamingWithRxJava() {
		DeployableStream s = XD.source(Sources.feed("http://feeds.bbci.co.uk/news/rss.xml")).
				process(Processors.jsonToTuple()).
				processRx(inputStream ->
						inputStream.map(tuple -> {
							return tuple.getValue("title").toString();
						}).map(data -> tuple().of("foo", data)))
				.sink(Sinks.log());
		s.deploy("foo", true);
	}

	public static void moreComplicatedRx() {
		DeployableStream s = XD.source(Sources.feed("http://feeds.bbci.co.uk/news/rss.xml")).
				process(Processors.filter("payload.toString().contains('sport')")).
				process(Processors.jsonToTuple()).
				processRx(inputStream ->
						inputStream.map(tuple -> {
							return tuple.getValue("title").toString();
						}).buffer(5).
								map(data -> tuple().of("new_news", mashup(data))))
				.process(Processors.transform("payload.toString().substring(82)")).sink(Sinks.log());
		s.deploy("foo", true);
	}

	// Mashup a few headlines into a new headline
	public static String mashup(List<String> ls) {
		List<String[]> arrays = new ArrayList<String[]>();
		for (int i = 0; i < ls.size(); i++) {
			arrays.add(ls.get(i).split(" "));
		}
		StringBuilder story = new StringBuilder();
		Random r = new Random(1);
		for (int i = 0; i < 10; i++) {
			String[] array = arrays.get(i % ls.size());
			story.append(array[(Math.abs(r.nextInt()) % array.length)]);
			story.append(" ");
		}
		return story.toString();
	}


	public static void lambdaSourceAndProcessor() {
		DeployableStream ds = XD.source(Examples::randomNumberGenerator).
				process(payload -> payload * 2).
				sink(Sinks.log());
		ds.deploy("foo", true);
	}

	public static Integer randomNumberGenerator() {
		XDUtils.sleep(500);
		return Math.abs(new java.util.Random().nextInt() % 100);
	}

	public static void wordsmith() {
		XD.source(Examples::letterProducer).sink(
				Sinks.custom("field-value-counter").setOption("fieldName", "letter")).deploy(
				"foo", true);
	}

	public static String letterProducer() {
		XDUtils.sleep(50);
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		return "{\"letter\":\"" + alphabet.charAt(Math.abs(new java.util.Random().nextInt() % 26)) + "\"}";
	}
}
