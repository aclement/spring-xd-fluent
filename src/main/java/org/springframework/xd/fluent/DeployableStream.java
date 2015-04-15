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

package org.springframework.xd.fluent;

import java.util.List;

import org.springframework.xd.fluent.domain.CodeProcessor;
import org.springframework.xd.fluent.domain.Processor;
import org.springframework.xd.fluent.domain.Resource;
import org.springframework.xd.fluent.domain.StreamState;
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

	public DeployableStream(StreamState streamState) {
		this.streamState = streamState;
		this.xdrc = XDRestClient.getInstance();
	}

	public void deploy() {
		deploy(null, false);
	}

	public void deploy(String streamName) {
		deploy(streamName, false);
	}

	private static boolean automaticallyTidyUpBeforeDeploy = false;

	/**
	 * @param streamName the name to use when deploying the stream
	 * @param replaceExistingStream if true, any existing stream of the same name will be destroyed prior to creating
	 *            this one
	 */
	public void deploy(String streamName, boolean replaceExistingStream) {
		XDRestClient xdrc = XDRestClient.getInstance();
		if (replaceExistingStream && streamName != null) {
			xdrc.streamDestroy(streamName);
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
				return;
			}
		}
		if (!streamState.isCreated()) {
			xdrc.streamCreate(streamName, streamState.toDSLString(), true);
		}
	}

	private boolean defineCodeModules() {
		System.out.println("defining new code modules");
		for (Processor processor : streamState.getProcessors()) {
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
		List<Resource> resourcesToPackage = streamState.getResourcesToPackageForModule(processor);
		byte[] customizedModule =
				CustomizedModuleGenerator.generate(processor.getType(), processorName,
						resourcesToPackage);
		//		System.out.println("  uploading processor: name = " + processorName + ", customized module size = "
		//				+ customizedModule.length + "bytes");
		//		return XDRestClient.moduleUpload(processorName, "/tmp/code-1.jar", "processor");
		boolean result = xdrc.moduleUpload(processorName, customizedModule, "processor");
		try {
			Thread.sleep(1000);
		}
		catch (Exception e) {
		}
		return result;
	}

}
