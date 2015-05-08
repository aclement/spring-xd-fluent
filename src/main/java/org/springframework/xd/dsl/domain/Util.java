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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import sun.reflect.ConstantPool;


/**
 * Common utils.
 *
 * @author aclement
 */
@SuppressWarnings("restriction")
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

	/*
	 * Sample data for the lambda: payload -> payload * 2
	 * LambdaCPSize = 36
		0 = Wrong type at constant pool index
		2 = class org.springframework.xd.dsl.examples.Everything$$Lambda$2/1323165413
		4 = class java.lang.Object
		6 = interface org.springframework.xd.dsl.domain.SerializableFunction
		8 = ()V
		10 = java/lang/Object,<init>,()V,
		12 = (Ljava/lang/Object;)Ljava/lang/Object;
		14 = class java.lang.Integer
		16 = class org.springframework.xd.dsl.examples.Everything
		18 = (Ljava/lang/Integer;)Ljava/lang/Integer;
		20 = org/springframework/xd/dsl/examples/Everything,lambda$1,(Ljava/lang/Integer;)Ljava/lang/Integer;,
		22 = ()Ljava/lang/Object;
		24 = class java.lang.invoke.SerializedLambda
		26 = Wrong type at constant pool index
		28 = Wrong type at constant pool index
		30 = Wrong type at constant pool index
		32 = (Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
		34 = java/lang/invoke/SerializedLambda,<init>,(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V,
	 */

	public static <T, R> Class getLambdaReturnType(SerializableFunction<T, R> processor) {
		try {
			Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
			getConstantPool.setAccessible(true);
			ConstantPool cpool = (ConstantPool) getConstantPool.invoke(processor.getClass());
			String[] memberRefInfo = cpool.getMemberRefInfoAt(20);
			// org/springframework/xd/dsl/examples/Everything,lambda$1,(Ljava/lang/Integer;)Ljava/lang/Integer;,
			org.objectweb.asm.Type t = org.objectweb.asm.Type.getReturnType(memberRefInfo[2]);
			String descriptor = t.getDescriptor();
			descriptor = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
			return Class.forName(descriptor);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T, R> Class getLambdaParameterType(SerializableFunction<T, R> processor) {
		try {
			//			Method[] ms = SerializableFunction.class.getMethods();
			//			for (Method m : ms) {
			//				Type[] ts = m.getGenericParameterTypes();
			//				System.out.println(m + " = " + toString(ts));
			//			}
			Method getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
			getConstantPool.setAccessible(true);
			ConstantPool cpool = (ConstantPool) getConstantPool.invoke(processor.getClass());
			//			System.out.println("LambdaCPSize = " + cpool.getSize());
			// 36
			//			for (int i = 0; i < cpool.getSize(); i += 2) {
			//				try {
			//					System.out.println(i + " = " + cpool.getUTF8At(i));
			//				}
			//				catch (Exception e) {
			//					try {
			//						System.out.println(i + " = " + cpool.getClassAt(i));
			//					}
			//					catch (Exception ee) {
			//						try {
			//							System.out.println(i + " = " + toString(cpool.getMemberRefInfoAt(i)));
			//						}
			//						catch (Exception eee) {
			//							System.out.println(i + " = " + e.getMessage());
			//						}
			//					}
			//				}
			//			}
			//			org.objectweb.asm.Type[] ts = org.objectweb.asm.Type.getArgumentTypes(cpool.getMemberRefInfoAt(cpool.getSize() - 2)[2]);
			//			for (int t = 0; t < ts.length; t++) {
			//				System.out.println("t" + t + " = " + ts[t]);
			//			}

			String[] memberRefInfo = cpool.getMemberRefInfoAt(20); // TODO is 20 reliable across JDKs?
			//			System.out.println(toString(memberRefInfo));
			// org/springframework/xd/dsl/examples/Everything,lambda$1,(Ljava/lang/Integer;)Ljava/lang/Integer;,
			org.objectweb.asm.Type t = org.objectweb.asm.Type.getArgumentTypes(memberRefInfo[2])[0];
			String descriptor = t.getDescriptor();
			descriptor = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
			return Class.forName(descriptor);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//	private static String toString(String[] memberRefInfoAt) {
	//		StringBuilder buf = new StringBuilder();
	//		for (String s : memberRefInfoAt) {
	//			buf.append(s).append(',');
	//		}
	//		return buf.toString();
	//	}

	//	private static String toString(Type[] memberRefInfoAt) {
	//		StringBuilder buf = new StringBuilder();
	//		for (Type s : memberRefInfoAt) {
	//			buf.append(s).append(',');
	//		}
	//		return buf.toString();
	//	}
}
