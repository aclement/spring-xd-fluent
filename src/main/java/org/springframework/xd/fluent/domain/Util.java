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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Common utils.
 *
 * @author aclement
 */
public class Util {

	public static byte[] loadBytesFromStream(InputStream stream) {
		try {
			BufferedInputStream bis = new BufferedInputStream(stream);
			byte[] theData = new byte[1000000];
			int dataReadSoFar = 0;
			byte[] buf = new byte[1024];
			int read = 0;
			while ((read = bis.read(buf)) != -1) {
				System.arraycopy(buf, 0, theData, dataReadSoFar, read);
				dataReadSoFar += read;
			}
			bis.close();
			byte[] returnData = new byte[dataReadSoFar];
			System.arraycopy(theData, 0, returnData, 0, dataReadSoFar);
			return returnData;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] loadClassAsBytes(String dottedClassname) {
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				dottedClassname.replace('.', '/') + ".class")) {
			if (is == null) {
				throw new IllegalStateException("Failed to find class " + dottedClassname
						+ " <- that should not have slashes in or end with .class!");
			}
			return loadBytesFromStream(is);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
