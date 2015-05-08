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

package org.springframework.xd.dsl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.xd.dsl.domain.BytesResource;
import org.springframework.xd.dsl.domain.ClassReference;
import org.springframework.xd.dsl.domain.CodeModule;
import org.springframework.xd.dsl.domain.CodeProcessor;
import org.springframework.xd.dsl.domain.CodeSource;
import org.springframework.xd.dsl.domain.CodeType;
import org.springframework.xd.dsl.domain.Processor;
import org.springframework.xd.dsl.domain.SerializableFunction;
import org.springframework.xd.dsl.domain.SerializableProcessor;
import org.springframework.xd.dsl.domain.SerializableSupplier;
import org.springframework.xd.dsl.domain.Sink;
import org.springframework.xd.dsl.domain.Source;
import org.springframework.xd.dsl.domain.Util;
import org.springframework.xd.dsl.internal.ClassFileProcessor;
import org.springframework.xd.dsl.internal.ClassFileProcessor.ClassMetaData;

/**
 * Represents a step in a stream definition. The generic type determines the type of the elements in this step of the
 * stream. The state of the stream is passed along between steps and accumulates information.
 *
 * @author aclement
 *
 */
public class PartialStream<T> {

	private StreamState state;

	private PartialStream() {
		state = new StreamState(null);
	}

	private PartialStream(Properties configuration) {
		state = new StreamState(configuration);
	}

	// ---

	static <V> PartialStream<V> newStream() {
		return new PartialStream<V>();
	}

	static <V> PartialStream<V> newStream(Properties configuration) {
		return new PartialStream<V>(configuration);
	}

	private <R> PartialStream<R> extend() {
		PartialStream<R> newStream = new PartialStream<R>(this.state);
		return newStream;
	}

	public <R> PartialStream<R> source(Source<R> source) {
		PartialStream<R> stream = PartialStream.<R> newStream();
		stream.setSource(source);
		return stream;
	}

	// Used to extend the stream with another step, increment the step counter
	private PartialStream(StreamState state) {
		this.state = state.copy();
		this.state.stepCount++;
	}


	private byte[] toBytes(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.close();
			byte[] bs = baos.toByteArray();
			return bs;
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public <R> PartialStream<R> processRx(SerializableProcessor<? super T, ? extends R> processor) {
		String fnClass = processor.getClass().getName();
		fnClass = fnClass.substring(0, fnClass.indexOf("$$Lambda"));
		state.usesCodeModules = true;
		byte[] serObject = toBytes(processor);
		CodeProcessor cp = new CodeProcessor(serObject, CodeType.RXJAVA_PROCESSOR, this.state.stepCount);
		addProcessor(cp);
		cp.addResource(new ClassReference("org.springframework.xd.dsl.domain.Source"));
		cp.addResource(new ClassReference("org.springframework.xd.dsl.domain.Module"));
		cp.addResource(new ClassReference(fnClass));
		// TODO find class refs in the fnClass
		cp.addResource(new ClassReference(SerializableProcessor.class.getName()));//"here.streamcomponents.SerializableProcessor"));
		cp.addResource(new BytesResource("lambda.ser", serObject));
		return this.<R> extend();
	}


	public <R> PartialStream<R> process(SerializableFunction<? super T, ? extends R> processor) {
		// Declaring type? processor.getClass().getName() example: here.Demo$$Lambda$1/1705736037
		String fnClass = processor.getClass().getName();
		fnClass = fnClass.substring(0, fnClass.indexOf("$$Lambda"));
		state.usesCodeModules = true;

		// Serialize it?
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(processor);
			oos.close();
			byte[] bs = baos.toByteArray();
			CodeProcessor cp = new CodeProcessor(bs, CodeType.JAVA_UTIL_FUNCTION_FUNCTION, this.state.stepCount);
			cp.setInputType(Util.getLambdaParameterType(processor));
			cp.setOutputType(Util.getLambdaReturnType(processor));
			addProcessor(cp);
			ClassMetaData cmd = ClassFileProcessor.getInfo(fnClass);
			List<String> typeRefs = cmd.getTypeReferences();
			System.out.println("Type refs = " + typeRefs);
			for (String typeRef : typeRefs) {
				cp.addResource(new ClassReference(typeRef.replace("/", ".")));
			}
			cp.addResource(new ClassReference("org.springframework.xd.dsl.domain.Source"));
			cp.addResource(new ClassReference("org.springframework.xd.dsl.domain.Module"));

			cp.addResource(new ClassReference(fnClass));
			// TODO find class refs in the fnClass
			cp.addResource(new ClassReference(SerializableFunction.class.getName()));//"here.streamcomponents.SerializableFunction"));
			cp.addResource(new BytesResource("lambda.ser", bs));
		}
		catch (IOException ioe) {

		}
		// this.addProcessor(new LambdaDrivenProcessor(processor));
		return this.<R> extend();
	}


	public <R> PartialStream<R> process(Processor<? super T, ? extends R> processor) {
		// The new stream step is configured based on the output type of the processor (and
		// thus indicates the type of elements on the stream now)
		addProcessor(processor);
		return this.<R> extend();
	}

	public DeployableStream sink(Sink<? super T> sink) {
		setSink(sink);
		state.isDeployable = true;
		// TODO return something immutable? (from an adding stages point of view)
		return new DeployableStream(this.state);
	}

	public void setSource(Source source) {
		this.state.source = source;
		if (source instanceof CodeModule) {
			this.state.usesCodeModules |= true;
		}
	}

	public void setSource(SerializableSupplier<T> source) {
		// Declaring type? processor.getClass().getName() example: here.Demo$$Lambda$1/1705736037
		String fnClass = source.getClass().getName();
		fnClass = fnClass.substring(0, fnClass.indexOf("$$Lambda"));

		System.out.println(fnClass);

		// org.springframework.xd.dsl.examples.Everything


		// Serialize it?
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(source);
			oos.close();
			byte[] bs = baos.toByteArray();
			CodeSource codeSource = new CodeSource(bs, CodeType.JAVA_UTIL_FUNCTION_SUPPLIER, 0);
			codeSource.addResource(new ClassReference(fnClass));
			// TODO find class refs in the fnClass
			ClassMetaData cmd = ClassFileProcessor.getInfo(fnClass);
			List<String> typeRefs = cmd.getTypeReferences();
			System.out.println("Type refs = " + typeRefs);
			for (String typeRef : typeRefs) {
				codeSource.addResource(new ClassReference(typeRef.replace("/", ".")));
			}
			codeSource.addResource(new ClassReference("org.springframework.xd.dsl.domain.Source"));
			codeSource.addResource(new ClassReference("org.springframework.xd.dsl.domain.Module"));
			codeSource.addResource(new ClassReference(SerializableSupplier.class.getName()));
			codeSource.addResource(new BytesResource("lambda.ser", bs));
			setSource(codeSource);
		}
		catch (IOException ioe) {

		}
	}

	public void setSink(Sink sink) {
		this.state.sink = sink;
	}

	public Source<?> getSource() {
		return this.state.source;
	}

	public void addProcessor(Processor<?, ?> processor) {
		if (this.state.processors == null) {
			this.state.processors = new ArrayList<Processor<?, ?>>();
		}
		this.state.processors.add(processor);
	}

	public Sink getSink() {
		return this.state.sink;
	}

	public List<Processor<?, ?>> getProcessors() {
		return this.state.processors;
	}

	public String toDSLString() {
		StringBuilder s = new StringBuilder();
		s.append(state.source.toDSLString());
		if (state.processors != null) {
			for (Processor processor : state.processors) {
				s.append(" | ");
				s.append(processor.toDSLString());
			}
		}
		s.append(" | ");
		s.append(state.sink.toDSLString());
		return s.toString();
	}

}
