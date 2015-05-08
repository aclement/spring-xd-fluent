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
 * Simple representation of a resource that is backed by a real class. The bytes are retrieved on request (and expected
 * to be found on the classpath).
 *
 * @author aclement
 *
 */
public class ClassReference implements Resource {

	private String classname;

	private byte[] bytes;

	// dot notation, e.g. foo.bar.SomeType
	public ClassReference(String dottedClassName) {
		this.classname = dottedClassName;
	}

	@Override
	public String getName() {
		return classname.replace('.', '/') + ".class";
	}

	@Override
	public byte[] getBytes() {
		if (this.bytes == null) {
			// Discover them
			this.bytes = Util.loadClassAsBytes(classname);
		}
		return this.bytes;
	}

	@Override
	public String toString() {
		return "ClassReference: " + classname;
	}

	@Override
	public int hashCode() {
		return 37 + classname.hashCode() * 37;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ClassReference) && ((ClassReference) obj).classname.equals(this.classname);
	}
}
