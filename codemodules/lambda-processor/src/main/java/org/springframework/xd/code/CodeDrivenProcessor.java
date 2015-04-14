/*
 * Copyright 2014 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.xd.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.function.Function;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.xd.tuple.Tuple;

/**
 * @author Andy Clement
 */
@MessageEndpoint
public abstract class CodeDrivenProcessor {

	protected Function fn;
	
	public CodeDrivenProcessor(String resource) {
		// 1. load the resource
		// 2. deserialize the lambda
		try {
//			InputStream is = new FileInputStream(new File("/tmp/bytes"));//
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
			ObjectInputStream ois = new ObjectInputStream(is);
			fn = (Function)ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	@Transformer(inputChannel = "input", outputChannel = "output")
	public String transformDriver(String payload) {
		return transform(payload);
	}

	@Transformer(inputChannel = "input", outputChannel = "output")
	public String transformDriver(Tuple payload) {
		return (String)fn.apply(payload);
	}

	String transform(String input) { // TODO Object/Object? not string?
		return (String)fn.apply(input);
	}

}
