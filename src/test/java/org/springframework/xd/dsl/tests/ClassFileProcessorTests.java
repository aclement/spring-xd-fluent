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

import java.util.List;

import org.junit.Test;

import org.springframework.xd.dsl.examples.Examples;
import org.springframework.xd.dsl.internal.ClassFileProcessor;
import org.springframework.xd.dsl.internal.ClassFileProcessor.ClassMetaData;


/**
 *
 * @author aclement
 */
public class ClassFileProcessorTests {

	@Test
	public void loadClass() {
		//		ClassMetaData cmd = ClassFileProcessor.getInfo(TestFixture.class.getName());
		ClassMetaData cmd = ClassFileProcessor.getInfo(Examples.class.getName());
		List<String> typeRefs = cmd.getTypeReferences();
		System.out.println(typeRefs);
	}
}


class TestFixture {

	String aaa;

	public void m(Runnable r) {

	}

	public void n() {
		m(() -> {
			System.out.println();
		});
	}
}
