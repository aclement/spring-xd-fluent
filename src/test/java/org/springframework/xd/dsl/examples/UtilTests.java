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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.springframework.xd.dsl.domain.Util;


/**
 *
 * @author aclement
 */
public class UtilTests {

	@Test
	public void lambdaReturnTypes() {
		Class clazz = Util.getLambdaReturnType((Integer it) -> it * 2);
		assertEquals(Integer.class, clazz);
		clazz = Util.getLambdaReturnType((String it) -> 35);
		assertEquals(Integer.class, clazz);
	}

	@Test
	public void lambdaParamTypes() {
		Class clazz = Util.getLambdaParameterType((Integer it) -> it * 2);
		assertEquals(Integer.class, clazz);
		clazz = Util.getLambdaParameterType((String it) -> 35);
		assertEquals(String.class, clazz);
	}
}
