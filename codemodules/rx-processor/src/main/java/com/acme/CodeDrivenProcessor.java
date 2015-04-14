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
package com.acme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.xd.rxjava.Processor;
import org.springframework.xd.tuple.Tuple;

import static org.springframework.xd.tuple.TupleBuilder.tuple;

import rx.Observable;

/**
 * @author Andy Clement
 */
public abstract class CodeDrivenProcessor implements Processor<Tuple,Tuple> {

	private static Log logger = LogFactory.getLog(Code.class);

	protected Processor<Tuple,Tuple> delegate;

	public CodeDrivenProcessor(String resource) {
		// 1. load the resource
		// 2. deserialize the lambda
		System.out.println("resource="+resource);
		if (resource == null) return;
		try {
			// InputStream is = new FileInputStream(new File("/tmp/bytes"));//
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream(resource);
			if (is == null) return;
			System.out.println("deserializing");
			ObjectInputStream ois = new ObjectInputStream(is);
			delegate = (Processor<Tuple,Tuple> )ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	@Override
	public Observable<Tuple> process(Observable<Tuple> inputStream) {
		return delegate.process(inputStream);
//		return inputStream.map(tuple -> { return tuple.getValue("title");}).map(data -> tuple().of("x",data));//.map(data -> tuple().of("wobble",data));
//		return fn.apply(inputStream);
//		return inputStream.map(tuple -> {
//			logger.info("Got data = " + tuple.toString());
//			return tuple.getDouble("measurement");
//		}).buffer(5).map(data -> tuple().of("average", avg(data)));
	}
	
}
