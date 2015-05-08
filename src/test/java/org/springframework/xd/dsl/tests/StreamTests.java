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

package org.springframework.xd.dsl.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.xd.dsl.DeployableStream;
import org.springframework.xd.dsl.DeployedStream;
import org.springframework.xd.dsl.StreamBuilder;
import org.springframework.xd.dsl.XD;
import org.springframework.xd.dsl.XDUtils;
import org.springframework.xd.dsl.domain.standard.Sinks;
import org.springframework.xd.dsl.domain.standard.Sources;
import org.springframework.xd.fluent.internal.XDRestClient;


/**
 *
 * @author aclement
 */
public class StreamTests {

	//	static XDRestClient xdrc = XDRestClient.getInstance();

	@BeforeClass
	public static void setUp() {
		XDRestClient.getInstance().destroyStream("foo");
	}

	@AfterClass
	public static void tearDown() {
	}

	@Test
	public void testSimpleStream() {
		DeployableStream deployableStream = XD.source(Sources.time()).sink(Sinks.log());
		DeployedStream deployedStream = deployableStream.deploy("foo");
		deployedStream.destroy();
	}

	@Test
	public void apiAlternatives() {
		//		// Concise
		//		XD.source(Sources.time()).sink(Sinks.log()).deploy("foo");
		//
		//		// Builder pattern with constructor
		//		new StreamBuilder().source(Sources.time()).sink(Sinks.log()).deploy("foo");
		//
		//		// Q. Can you re-use a stream builder? Or is it one per stream?
		//		// A. Can re-use it to build a new stream
		//
		//		StreamBuilder configuredStreamBuilder = new StreamBuilder(configuration);
		//		// these are two different streams
		//		configuredStreamBuilder.source(time()).sink(Sinks.log()).deploy("foo");
		//		configuredStreamBuilder.source(time()).sink(Sinks.log()).deploy("foo");
		//
		//
		//		StreamBuilderFactory sbf = new StreamBuilderFactory(configurationProperties);
		//		sbf.getStreamBuilder();

	}

	@Test
	public void codeProcessor() {
		DeployableStream ds = XD.source(Sources.time()).process(payload -> payload.substring(4)).sink(Sinks.log());
		ds.deploy("foo");
	}

	@Test
	public void codeSource() {
		//		DeployableStream ds = XD.source(StreamTests::randomNumberGenerator).sink(Sinks.log());
		//		ds.deploy("foo");

		DeployableStream ds = new StreamBuilder().source(Sources.time()).sink(Sinks.log());
		ds.deploy("foo", true);
	}

	public static Object randomNumberGenerator() {
		XDUtils.sleep(500);
		return Math.abs(new java.util.Random().nextInt() % 100);
	}

}
