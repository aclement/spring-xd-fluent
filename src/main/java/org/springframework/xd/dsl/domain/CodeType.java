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

package org.springframework.xd.dsl.domain;

/**
 * The two types of code driven processors supported right now. Basic lambda (java.util.Function) and RxJava.
 *
 * @author aclement
 *
 */
public enum CodeType {
	JAVA_UTIL_FUNCTION_SUPPLIER, JAVA_UTIL_FUNCTION_FUNCTION, RXJAVA_PROCESSOR;
}
