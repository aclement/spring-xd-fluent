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
import java.util.Collection;

import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.client.RestTemplate;
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


	private XDRestClient() {
	}

	public static Collection<StreamDefinitionResource> listStreams() {
		return xdTemplate.streamOperations().list().getContent();
	}

	public static Collection<ModuleDefinitionResource> listModules() {
		PagedResources<ModuleDefinitionResource> o = xdTemplate.moduleOperations().list(null);
		return o.getContent();
	}

	public static int destroyCodeStreams() {
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

	public static int deleteCodeModules() {
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

	public static boolean moduleUpload(String moduleName, byte[] moduleContents, String type) {
		try {
			// TODO why on earth can't I use a ByteArrayResource on that API call? (returns server 500)
			File f = new File(System.getProperty("java.io.tmpdir") + File.separator
					+ "code.jar");
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

	public static boolean moduleUpload(String moduleName, String modulePath, String type) {
		try {
			restTemplate.postForObject(xdUrl + "modules/{type}/{moduleName}", new FileSystemResource(new File(
					modulePath)),
					String.class, type, moduleName);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean createStream(String name, String definition, boolean deploy) {
		System.out.println("Creating stream: '" + name + "=" + definition + "' (deploy=" + deploy + ")");
		xdTemplate.streamOperations().createStream(name, definition, deploy);
		return true;
	}

}
