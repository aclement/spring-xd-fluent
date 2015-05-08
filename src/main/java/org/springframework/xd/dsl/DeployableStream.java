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

import java.util.Set;

import org.springframework.xd.dsl.domain.CodeModule;
import org.springframework.xd.dsl.domain.Processor;
import org.springframework.xd.dsl.domain.Resource;
import org.springframework.xd.fluent.internal.CustomizedModuleGenerator;
import org.springframework.xd.fluent.internal.XDRestClient;

/**
 * Represents a deployable stream (a deployable one is a source, then some number of processors, then a sink).
 *
 * @author aclement
 *
 */
public class DeployableStream {

	// The state passed in the constructor to this type is a well formed stream
	StreamState streamState;

	XDRestClient xdrc;

	private String streamName;

	public DeployableStream(StreamState streamState) {
		this.streamState = streamState;
		this.xdrc = XDRestClient.getInstance();
	}

	public DeployedStream deploy(String streamName) {
		return deploy(streamName, false);
	}

	private static boolean automaticallyTidyUpBeforeDeploy = false;

	/**
	 * @param streamName the name to use when deploying the stream
	 * @param replaceExistingStream if true, any existing stream of the same name will be destroyed prior to creating
	 *            this one
	 */
	public DeployedStream deploy(String streamName, boolean replaceExistingStream) {
		this.streamName = (streamName == null ? "anonymous" : streamName);
		XDRestClient xdrc = XDRestClient.getInstance();
		if (replaceExistingStream) {
			xdrc.destroyStream(this.streamName);
			xdrc.deleteCodeModulesForStream(this.streamName);
		}
		if (automaticallyTidyUpBeforeDeploy) {
			xdrc.destroyCodeStreams();
			xdrc.deleteCodeModules();
		}
		if (streamState.usesCodeModules()) {
			// Package and upload those modules
			// TODO deal with sources/sinks
			if (!defineCodeModules()) {
				System.out.println("Failed to deploy code modules, exiting");
				return null;
			}
		}
		if (!streamState.isCreated()) {
			xdrc.createStream(this.streamName, streamState.toDSLString(), true);
		}
		return new DeployedStream(this.streamName, streamState);
	}

	private boolean defineCodeModules() {
		System.out.println("defining new code modules");
		if (streamState.getSource() instanceof CodeModule) {
			if (!defineCodeModule((CodeModule) streamState.getSource())) {
				return false;
			}
		}
		if (streamState.getProcessors() != null) {
			for (Processor processor : streamState.getProcessors()) {
				if (processor instanceof CodeModule) {
					if (!defineCodeModule((CodeModule) processor)) {
						return false;
					}
				}
			}
		}
		if (streamState.getSink() instanceof CodeModule) {
			if (!defineCodeModule((CodeModule) streamState.getSink())) {
				return false;
			}
		}
		return true;
	}

	private boolean defineCodeModule(CodeModule module) {
		module.setStreamName(this.streamName);
		String moduleName = module.getName();
		System.out.println("  building custom code processor module: " + moduleName);
		Set<Resource> resourcesToPackage = module.getResourcesToPackage();
		byte[] customizedModule =
				CustomizedModuleGenerator.generate(module.getType(), module.getModuleType(), moduleName,
						resourcesToPackage, module.getInputType(), module.getOutputType());
		//		System.out.println("  uploading processor: name = " + processorName + ", customized module size = "
		//				+ customizedModule.length + "bytes");
		boolean result = xdrc.uploadModule(moduleName, customizedModule, module.getModuleType().toString());
		// TODO is it necessary to make sure?
		if (result) {
			xdrc.waitOnModuleExistence(module.getModuleType().toString(), moduleName);
		}
		return result;
	}

}
