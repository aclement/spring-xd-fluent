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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import org.springframework.xd.dsl.PartialStream;
import org.springframework.xd.dsl.XD;
import org.springframework.xd.dsl.domain.Source;
import org.springframework.xd.dsl.domain.standard.Sources;


/**
 *
 * @author aclement
 */
public class PartialStreamTests {

	@Test
	public void testProcessors() {
		PartialStream<String> ps = XD.source(Sources.feed("http://wibble"));
		Source<?> s = ps.getSource();
		assertEquals("feed", s.getName());
		Map<String, String> options = s.getOptions();
		assertEquals(1, options.size());
		assertTrue(options.containsKey("url"));
		assertEquals("http://wibble", options.get("url"));
	}
}
