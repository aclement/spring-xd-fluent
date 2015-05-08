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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.springframework.xd.dsl.domain.CodeType;
import org.springframework.xd.dsl.domain.ModuleType;
import org.springframework.xd.dsl.domain.Resource;


/**
 * Customize an almost deployable module into a new jar by adding the missing pieces, producing a jar that can be
 * deployed.
 *
 * @author aclement
 */
public class CustomizedModuleGenerator implements Opcodes {

	public static Map<CodeType, Map<ModuleType, String>> jarMap = new HashMap<>();

	static {
		Map<ModuleType, String> moduleToJarMap = new HashMap<>();
		moduleToJarMap.put(ModuleType.processor,
				"codemodules/lambda-processor/target/lambda-processor-1.0.0.BUILD-SNAPSHOT.jar");
		jarMap.put(CodeType.JAVA_UTIL_FUNCTION_FUNCTION, moduleToJarMap);

		moduleToJarMap = new HashMap<>();
		moduleToJarMap.put(ModuleType.source,
				"codemodules/lambda-source/target/lambda-source-1.0.0.BUILD-SNAPSHOT.jar");
		jarMap.put(CodeType.JAVA_UTIL_FUNCTION_SUPPLIER, moduleToJarMap);

		moduleToJarMap = new HashMap<>();
		moduleToJarMap.put(ModuleType.processor,
				"codemodules/rx-processor/target/rx-processor-1.0.0.BUILD-SNAPSHOT.jar");
		jarMap.put(CodeType.RXJAVA_PROCESSOR, moduleToJarMap);
	}

	public static String LAMBDA_PROCESSOR = "codemodules/lambda-processor/target/lambda-processor-1.0.0.BUILD-SNAPSHOT.jar";

	public static String RX_PROCESSOR =
			//			"/Users/aclement/workspaces/xdplay/rxjava-moving-average/target/rxjava-moving-average-1.0.0.BUILD-SNAPSHOT.jar";

			"codemodules/rx-processor/target/rx-processor-1.0.0.BUILD-SNAPSHOT.jar";

	public static byte[] generate(CodeType type, ModuleType moduleType, String moduleName,
			Set<Resource> resourcesToInclude, Class inputType, Class outputType) {
		ByteArrayOutputStream outputModuleStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		try {
			InputStream is = null;
			Map<ModuleType, String> moduleMap = jarMap.get(type);
			String inputJarFile = moduleMap.get(moduleType);
			try {
				is = new FileInputStream(new File(inputJarFile));
			}
			catch (FileNotFoundException fnfe) {
				inputJarFile = "codemodules" + inputJarFile.substring(inputJarFile.lastIndexOf("/"));
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(inputJarFile);
			}
			//			if (type == CodeType.JAVA_UTIL_FUNCTION_FUNCTION) {
			//				try {
			//					is = new FileInputStream(new File(LAMBDA_PROCESSOR));
			//				}
			//				catch (FileNotFoundException fnfe) {
			//					// running in jar mode
			//					System.out.println("Accessing lambda-processor template jar via classloader");
			//					is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
			//							"codemodules/lambda-processor-1.0.0.BUILD-SNAPSHOT.jar");
			//				}
			//			}
			//			else {
			//				try {
			//					is = new FileInputStream(new File(RX_PROCESSOR));
			//				}
			//				catch (FileNotFoundException fnfe) {
			//					// running in jar mode
			//					System.out.println("Accessing rx-processor template jar via classloader");
			//					is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
			//							"codemodules/rx-processor-1.0.0.BUILD-SNAPSHOT.jar");
			//				}
			//			}
			ZipInputStream zis = new ZipInputStream(is);
			ZipOutputStream zos = new ZipOutputStream(outputModuleStream);
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				ZipEntry newZipEntry = new ZipEntry(ze.getName());
				if (ze.getName().endsWith(".jar")) {
					System.out.println("got " + ze.getName());
					newZipEntry.setMethod(ZipEntry.STORED);
					newZipEntry.setCrc(ze.getCrc());
					newZipEntry.setCompressedSize(ze.getCompressedSize());
					newZipEntry.setSize(ze.getSize());
				}
				zos.putNextEntry(newZipEntry); // problem with the size being set already on ze?
				// Only the module type processor currently generates a specialized Code class
				// (which replaces the default one defined in the codemodule for lambda processor)
				if (type == CodeType.JAVA_UTIL_FUNCTION_FUNCTION && moduleType == ModuleType.processor
						&& ze.getName().equals("org/springframework/xd/code/Code.class")) {
					writeCustomCode(zos, toDescriptor(inputType), toDescriptor(outputType));
				}
				else {
					System.out.println("copying " + ze.getName());
					int len = 0;
					while ((len = zis.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				}
			}
			for (Resource r : resourcesToInclude) {
				System.out.println("  ...including resource " + r.getName());
				ZipEntry newZipEntry = new ZipEntry(r.getName());
				zos.putNextEntry(newZipEntry);
				zos.write(r.getBytes());
			}
			zis.close();
			zos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return outputModuleStream.toByteArray();
	}

	/**
	 * @param outputType
	 * @return
	 */
	private static String toDescriptor(Class clazz) {
		if (clazz == null)
			return null;
		String descriptor = "L" + clazz.getName().replace('.', '/') + ";";
		return descriptor;
	}

	//	// class version 52.0 (52)
	//	// access flags 0x21
	//	public class org/springframework/xd/code/Code extends org/springframework/xd/code/CodeDrivenProcessor  {
	//	  // access flags 0x1
	//	  public <init>()V
	//	   L0
	//	    LINENUMBER 6 L0
	//	    ALOAD 0
	//	    INVOKESPECIAL org/springframework/xd/code/CodeDrivenProcessor.<init> ()V
	//	   L1
	//	    LINENUMBER 7 L1
	//	    RETURN
	//	   L2
	//	    LOCALVARIABLE this Lorg/springframework/xd/code/Code; L0 L2 0
	//	    MAXSTACK = 1
	//	    MAXLOCALS = 1
	//
	//	  // access flags 0x1
	//	  public transform(Ljava/lang/Integer;)Ljava/lang/Integer;
	//	   L0
	//	    LINENUMBER 10 L0
	//	    ALOAD 0
	//	    GETFIELD org/springframework/xd/code/Code.fn : Ljava/util/function/Function;
	//	    ALOAD 1
	//	    INVOKEINTERFACE java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object;
	//	    CHECKCAST java/lang/Integer
	//	    ARETURN
	//	   L1
	//	    LOCALVARIABLE this Lorg/springframework/xd/code/Code; L0 L1 0
	//	    LOCALVARIABLE input Ljava/lang/Integer; L0 L1 1
	//	    MAXSTACK = 2
	//	    MAXLOCALS = 2
	//	}
	/**
	 * @param inDescriptor input descriptor of the form Lfoo/Bar;
	 * @param outDescriptor output descriptor of the form Lfoo/Boo;
	 */
	private static void writeCustomCode(ZipOutputStream zos, String inDescriptor, String outDescriptor) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "org/springframework/xd/code/Code", null,
				"org/springframework/xd/code/CodeDrivenProcessor", null);

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "org/springframework/xd/code/CodeDrivenProcessor", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		// An example transform method that picks even number chars from the input payload and makes that into the output
		mv = cw.visitMethod(ACC_PUBLIC, "transform", "(" + inDescriptor + ")" + outDescriptor, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, "org/springframework/xd/code/Code", "fn", "Ljava/util/function/Function;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Function", "apply",
				"(Ljava/lang/Object;)Ljava/lang/Object;", true);
		mv.visitTypeInsn(CHECKCAST, outDescriptor.substring(1, outDescriptor.length() - 1));//"java/lang/Integer");
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
		cw.visitEnd();
		try {
			zos.write(cw.toByteArray());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
