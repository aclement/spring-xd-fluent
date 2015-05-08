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

package org.springframework.xd.fluent.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.client.RestTemplate;
import org.springframework.xd.dsl.domain.Util;
import org.springframework.xd.rest.client.impl.SpringXDException;
import org.springframework.xd.rest.client.impl.SpringXDTemplate;
import org.springframework.xd.rest.domain.ModuleDefinitionResource;
import org.springframework.xd.rest.domain.RESTModuleType;
import org.springframework.xd.rest.domain.StreamDefinitionResource;


/**
 * Helper methods for calling the XD REST API.
 *
 * @author aclement
 */
public class XDRestClient {

	private static String xdUrl = null;

	private static SpringXDTemplate xdTemplate = null;

	private static RestTemplate restTemplate = new RestTemplate();

	static {
		try {
			xdUrl = System.getProperty("xd.api", "http://localhost:9393");
			xdTemplate = new SpringXDTemplate(new URI(xdUrl));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static XDRestClient instance;

	private XDRestClient() {
	}

	public static XDRestClient getInstance() {
		if (instance == null) {
			instance = new XDRestClient();
		}
		return instance;
	}

	public Collection<StreamDefinitionResource> listStreams() {
		return xdTemplate.streamOperations().list().getContent();
	}

	public Collection<StreamDefinitionResource> listStreams(StreamDefinitionResourceFilter filter) {
		List<StreamDefinitionResource> results = new ArrayList<>();
		for (StreamDefinitionResource sdr : xdTemplate.streamOperations().list().getContent()) {
			if (filter.accept(sdr)) {
				results.add(sdr);
			}
		}
		return (results.size() == 0 ? Collections.emptyList() : results);
	}

	public Collection<ModuleDefinitionResource> listModules() {
		PagedResources<ModuleDefinitionResource> o = xdTemplate.moduleOperations().list(null);
		return o.getContent();
	}


	public Collection<ModuleDefinitionResource> listModules(ModuleFilter moduleFilter) {
		PagedResources<ModuleDefinitionResource> o = xdTemplate.moduleOperations().list(null);
		List<ModuleDefinitionResource> results = new ArrayList<>();
		for (ModuleDefinitionResource moduleDefinitionResource : o.getContent()) {
			//			System.out.println("Checking '" + moduleDefinitionResource.getName() + "' against filter");
			if (moduleFilter.accept(moduleDefinitionResource)) {
				results.add(moduleDefinitionResource);
			}
		}
		return (results.size() == 0 ? Collections.emptyList() : results);
	}

	public boolean destroyStream(String streamName) {
		return destroyStream(streamName, true);
	}

	public boolean destroyStream(String streamName, boolean destroyRelatedCodeModules) {
		boolean retVal = false;
		try {
			System.out.println("Destroying stream '" + streamName + "'");
			xdTemplate.streamOperations().destroy(streamName);
			retVal = waitOnStreamDisappearance(streamName);
		}
		catch (SpringXDException e) {
			if (e.getMessage().startsWith("There is no stream definition named ")) {
				return false;
			}
			else {
				throw e;
			}
		}
		finally {
			if (destroyRelatedCodeModules) {
				deleteCodeModulesForStream(streamName);
			}
		}
		return retVal;
	}

	public int destroyCodeStreams() {
		System.out.println("destroying old code streams");
		int count = 0;
		Collection<StreamDefinitionResource> streams = listStreams();
		for (StreamDefinitionResource resource : streams) {
			if (resource.getName().startsWith("code")) {
				System.out.println("  destroying stream: " + resource.getName());
				xdTemplate.streamOperations().destroy(resource.getName());
				count++;
			}
		}
		if (count != 0) {
			// TODO wait a moment to ensure it is done, guess we could list again to verify gone
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
			}
		}
		return count;
	}

	public int deleteCodeModules() {
		System.out.println("deleting old code modules");
		int count = 0;
		Collection<ModuleDefinitionResource> modules = listModules();
		for (ModuleDefinitionResource mdr : modules) {
			if (mdr.getName().startsWith("code")) {
				System.out.println("  deleting module: " + mdr.getName());
				xdTemplate.moduleOperations().deleteModule(mdr.getName(), RESTModuleType.valueOf(mdr.getType()));
				count++;
			}
		}
		return count;
	}

	/**
	 * Code modules for a stream are named according to the pattern "&lt;streamname&gt;-code-&lt;index&gt;" so it is
	 * possible to find them easily.
	 *
	 * @param streamName the name of the stream for which modules must be deleted
	 * @return the number of code modules deleted
	 */
	public int deleteCodeModulesForStream(String streamName) {
		int count = 0;
		Collection<ModuleDefinitionResource> modules =
				listModules(mdr -> mdr.getName().startsWith(streamName + "-code-"));
		for (ModuleDefinitionResource mdr : modules) {
			System.out.println("Deleting code module '" + mdr.getName() + "'");
			xdTemplate.moduleOperations().deleteModule(mdr.getName(), RESTModuleType.valueOf(mdr.getType()));
			count++;
		}
		return count;
	}

	public boolean uploadModule(String moduleName, byte[] moduleContents, String type) {
		try {
			// TODO why on earth can't I use a ByteArrayResource on that API call? (returns server 500)
			File f = new File(System.getProperty("java.io.tmpdir") + File.separator
					+ "code.jar");
			System.out.println(f.getPath());
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(moduleContents);
			fos.close();
			return moduleUpload(moduleName, f.toString(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean moduleUpload(String moduleName, String modulePath, String type) {
		try {
			restTemplate.postForObject(xdUrl + "modules/{type}/{moduleName}", new FileSystemResource(new File(
					modulePath)),
					String.class, type, moduleName);
			waitOnModuleExistence(type, moduleName);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean createStream(String name, String definition, boolean deploy) {
		System.out.println("Creating stream: '" + name + "=" + definition + "' (deploy=" + deploy + ")");
		xdTemplate.streamOperations().createStream(name, definition, deploy);
		return waitOnStreamExistence(name, deploy ? StreamState.DEPLOYED : null);
	}

	enum StreamState {
		DEPLOYED, DEPLOYING;
	}

	/**
	 * Check if a stream exists in the specified state. If the state doesn't matter, pass null as the desired state.
	 *
	 * @param streamName the stream name to check for
	 * @param desiredState if non-null, method will only succeed if the stream is in the desired state
	 *
	 * @return true if the stream exists and is the desired state
	 */
	public boolean checkStreamExists(String streamName, StreamState desiredState) {
		Collection<StreamDefinitionResource> streams = listStreams();
		for (StreamDefinitionResource stream : streams) {
			if (stream.getName().equals(streamName)
					&& (desiredState == null || stream.getStatus().equalsIgnoreCase(desiredState.toString()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if the module exists
	 */
	public boolean checkModuleExists(String type, String moduleName) {
		try {
			xdTemplate.moduleOperations().info(moduleName, RESTModuleType.valueOf(type));
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @return true if the stream does not exist
	 */
	public boolean checkStreamDoesNotExist(String streamName) {
		Collection<StreamDefinitionResource> streams = listStreams();
		for (StreamDefinitionResource stream : streams) {
			if (stream.getName().equals(streamName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Waits up to 5 seconds for a stream to appear as available on the server.
	 *
	 * @param streamName the name of the stream to wait for
	 * @param desiredState if non null wait for the named stream to be in this state
	 */
	private boolean waitOnStreamExistence(String streamName, StreamState desiredState) {
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis() - time) < 5000) {
			boolean exists = checkStreamExists(streamName, desiredState);
			if (exists) {
				return true;
			}
			Util.sleep(200);
		}
		return false;
	}


	/**
	 * Waits up to 5 seconds for a module to appear as available on the server.
	 */
	public boolean waitOnModuleExistence(String type, String moduleName) {
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis() - time) < 5000) {
			boolean exists = checkModuleExists(type, moduleName);
			if (exists) {
				return true;
			}
			Util.sleep(200);
		}
		return false;
	}

	/**
	 * Waits up to 5 seconds for a stream to disappear from the server.
	 */
	private boolean waitOnStreamDisappearance(String streamName) {
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis() - time) < 5000) {
			boolean exists = checkStreamExists(streamName, null);
			if (!exists) {
				return true;
			}
			Util.sleep(200);
		}
		return false;
	}

	@FunctionalInterface
	static interface ModuleFilter {

		boolean accept(ModuleDefinitionResource moduleDefinitionResource);
	}

	@FunctionalInterface
	interface StreamDefinitionResourceFilter {

		boolean accept(StreamDefinitionResource streamDefinitionResource);
	}
}
