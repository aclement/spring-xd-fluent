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

package org.springframework.xd.fluent.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.xd.fluent.internal.XDRestClient;


/**
 * @author aclement
 */
public class XDRestClientTests {

	static XDRestClient xdrc = XDRestClient.getInstance();

	@BeforeClass
	public static void setUp() {
		xdrc.streamDestroy("does-not-exist");
		xdrc.streamDestroy("foobar");
	}

	@Test
	public void streamOperations() {
		assertFalse(xdrc.streamDestroy("does-not-exist"));
		assertTrue(xdrc.streamCreate("foobar", "time | log", false));
		assertTrue(xdrc.streamExists("foobar"));
		assertFalse(xdrc.streamDoesNotExist("foobar"));
		assertTrue(xdrc.streamDestroy("foobar"));
		assertFalse(xdrc.streamDestroy("foobar"));
		assertFalse(xdrc.streamExists("foobar"));
		assertTrue(xdrc.streamDoesNotExist("foobar"));
	}

	@Test
	public void moduleOperations() {
		assertFalse(xdrc.moduleExists("processor", "foobar"));
		assertTrue(xdrc.moduleExists("source", "mail"));
	}
}
