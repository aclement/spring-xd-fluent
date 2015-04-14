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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.xd.fluent.DeployableStream;
import org.springframework.xd.fluent.internal.CustomizedModuleGenerator;
import org.springframework.xd.fluent.internal.XDRestClient;

/**
 * Represents a step in a stream definition. The generic type determines the type of the elements in this step of the
 * stream. The state of the stream is passed along between steps and accumulates information.
 *
 * @author aclement
 *
 */
public class StreamStep<T> {

	/**
	 * Encapsulates the state of a stream and accumulates information as stream steps occur.
	 */
	static class StreamState {

		private String name;

		private Source<?> source;

		private List<Processor<?, ?>> processors;

		private Sink<?> sink;

		// TODO deployable when sink is added
		private boolean isDeployable = false;

		private boolean isCreated = false;

		private Map<String, List<Resource>> resourcesToPackagePerModule;

		private boolean usesCodeModules = false;
	}

	private StreamState state;

	public static <V> StreamStep<V> newStream() {
		return new StreamStep<V>();
	}

	private StreamStep() {
		state = new StreamState();
	}

	private <R> StreamStep<R> extend() {
		StreamStep<R> newStream = new StreamStep<R>();
		newStream.state = this.state;
		return newStream;
	}


	private void addResource(String moduleName, Resource resource) {
		if (state.resourcesToPackagePerModule == null) {
			state.resourcesToPackagePerModule = new HashMap<String, List<Resource>>();
		}
		List<Resource> list = null;
		list = state.resourcesToPackagePerModule.get(moduleName);
		if (list == null) {
			list = new ArrayList<Resource>();
			state.resourcesToPackagePerModule.put(moduleName, list);
		}
		list.add(resource);
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


	public StreamStep procezz(SerializableProcessor processor) {
		byte[] serObject = toBytes(processor);
		try {
			FileOutputStream fos = new FileOutputStream("/Users/aclement/lambda.ser");
			fos.write(serObject);
			fos.close();
		}
		catch (Exception e) {

		}
		return this;
	}

	public <R> StreamStep<R> processrx(SerializableProcessor<? super T, ? extends R> processor) {
		String fnClass = processor.getClass().getName();
		fnClass = fnClass.substring(0, fnClass.indexOf("$$Lambda"));
		state.usesCodeModules = true;
		byte[] serObject = toBytes(processor);
		CodeProcessor cp = new CodeProcessor(serObject, CodeProcessorType.PROCESSOR);
		addProcessor(cp);
		addResource(cp.getName(), new ClassReference(fnClass));
		// TODO find class refs in the fnClass
		addResource(cp.getName(), new ClassReference(SerializableProcessor.class.getName()));//"here.streamcomponents.SerializableProcessor"));
		addResource(cp.getName(), new BytesResource("lambda.ser", serObject));
		return this.<R> extend();
	}

	public <R> StreamStep<R> process(SerializableFunction<? super T, ? extends R> processor) {
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
			//			testIt(bs);
			CodeProcessor cp = new CodeProcessor(bs, CodeProcessorType.FUNCTION);
			addProcessor(cp);
			addResource(cp.getName(), new ClassReference(fnClass));
			// TODO find class refs in the fnClass
			addResource(cp.getName(), new ClassReference(SerializableFunction.class.getName()));//"here.streamcomponents.SerializableFunction"));
			addResource(cp.getName(), new BytesResource("lambda.ser", bs));
		}
		catch (IOException ioe) {

		}
		// this.addProcessor(new LambdaDrivenProcessor(processor));
		return this.<R> extend();
	}

	public <R> StreamStep<R> process(Processor<? super T, ? extends R> processor) {
		// The new stream step is configured based on the output type of the processor (and
		// thus indicates the type of elements on the stream now)
		addProcessor(processor);
		return this.<R> extend();
	}

	public DeployableStream sink(Sink<? super T> sink) {
		setSink(sink);
		state.isDeployable = true;
		// TODO return something immutable? (from an adding stages point of view)
		return new DeployableStream(this);
	}

	public void setSource(Source source) {
		this.state.source = source;
	}

	public void setSink(Sink sink) {
		this.state.sink = sink;
	}

	public Source getSource() {
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

	public static int counter = 1;

	public String getName() {
		if (this.state.name == null) {
			this.state.name = "code-stream-" + Integer.toString(counter++);
		}
		return this.state.name;
	}

	// TODO retval?
	public void create(boolean deploy) {
		XDRestClient.createStream(getName(), toDSLString(), deploy);
	}

	private boolean defineCodeModules() {
		System.out.println("defining new code modules");
		for (Processor processor : state.processors) {
			if (processor instanceof CodeProcessor) {
				if (!defineCodeModule((CodeProcessor) processor)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean defineCodeModule(CodeProcessor processor) {
		String processorName = processor.getName();
		System.out.println("  building custom code processor module: " + processorName);
		List<Resource> resourcesToPackage = state.resourcesToPackagePerModule.get(processorName);
		byte[] customizedModule =
				CustomizedModuleGenerator.generate(processor.getType(), processorName,
						resourcesToPackage);
		//		System.out.println("  uploading processor: name = " + processorName + ", customized module size = "
		//				+ customizedModule.length + "bytes");
		//		return XDRestClient.moduleUpload(processorName, "/tmp/code-1.jar", "processor");
		boolean result = XDRestClient.moduleUpload(processorName, customizedModule, "processor");
		try {
			Thread.sleep(1000);
		}
		catch (Exception e) {
		}
		return result;
	}

	public void deploy() {
		System.out.println("Preparing to deploy: " + toDSLString());
		XDRestClient.destroyCodeStreams();
		if (state.usesCodeModules) {
			XDRestClient.deleteCodeModules();
			// Package and upload those modules
			// TODO deal with sources/sinks
			if (!defineCodeModules()) {
				System.out.println("Failed to deploy code modules, exiting");
				return;
			}
		}
		if (!state.isCreated) {
			create(true);
		}
		if (!state.isDeployable) {
			throw new UnsupportedOperationException("no way!");
		}

	}

}
