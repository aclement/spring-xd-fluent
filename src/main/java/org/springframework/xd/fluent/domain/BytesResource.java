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

package org.springframework.xd.fluent.domain;

/**
 * Simple representation of a byte array based resource.
 * @author aclement
 *
 */
class BytesResource implements Resource {

	private String name;

	private byte[] bytes;

	BytesResource(String name, byte[] bytes) {
		this.name = name;
		this.bytes = bytes;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getBytes() {
		return this.bytes;
	}
}