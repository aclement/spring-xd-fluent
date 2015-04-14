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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.Opcodes;

import org.springframework.xd.fluent.domain.CodeProcessorType;
import org.springframework.xd.fluent.domain.Resource;


/**
 * Customize an almost deployable module into a new jar by adding the missing pieces, producing a jar that can be
 * deployed.
 *
 * @author aclement
 */
public class CustomizedModuleGenerator implements Opcodes {

	public static String LAMBDA_PROCESSOR = "codemodules/lambda-processor/target/lambda-processor-1.0.0.BUILD-SNAPSHOT.jar";

	public static String RX_PROCESSOR =
			//			"/Users/aclement/workspaces/xdplay/rxjava-moving-average/target/rxjava-moving-average-1.0.0.BUILD-SNAPSHOT.jar";

			"codemodules/rx-processor/target/rx-processor-1.0.0.BUILD-SNAPSHOT.jar";

	public static byte[] generate(CodeProcessorType type, String processorName, List<Resource> resourcesToInclude) {
		ByteArrayOutputStream outputModuleStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		try {
			InputStream is = null;
			if (type == CodeProcessorType.FUNCTION) {
				try {
					is = new FileInputStream(new File(LAMBDA_PROCESSOR));
				}
				catch (FileNotFoundException fnfe) {
					// running in jar mode
					System.out.println("Accessing lambda-processor template jar via classloader");
					is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"codemodules/lambda-processor-1.0.0.BUILD-SNAPSHOT.jar");
				}
			}
			else {
				try {
					is = new FileInputStream(new File(RX_PROCESSOR));
				}
				catch (FileNotFoundException fnfe) {
					// running in jar mode
					System.out.println("Accessing rx-processor template jar via classloader");
					is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"codemodules/rx-processor-1.0.0.BUILD-SNAPSHOT.jar");
				}
			}
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
				// if (ze.getName().equals("org/springframework/xd/code/Code.class")) {
				//		writeCustomCode(zos);
				// }
				// else {
				//		System.out.println("copying " + ze.getName());
				int len = 0;
				while ((len = zis.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				// }
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
		}
		return outputModuleStream.toByteArray();
	}
	/**
	 * Not used right now, was an experiment when thinking about generating the contents of the deployable module.
	 */
	//	private static void writeCustomCode(ZipOutputStream zos) {
	//		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
	//		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "org/springframework/xd/code/Code", null,
	//				"org/springframework/xd/code/CodeDrivenProcessor", null);
	//
	//		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
	//		mv.visitCode();
	//		mv.visitVarInsn(ALOAD, 0);
	//		mv.visitMethodInsn(INVOKESPECIAL, "org/springframework/xd/code/CodeDrivenProcessor", "<init>", "()V", false);
	//		mv.visitInsn(RETURN);
	//		mv.visitMaxs(1, 1);
	//		mv.visitEnd();
	//
	//		// An example transform method that picks even number chars from the input payload and makes that into the output
	//		mv = cw.visitMethod(ACC_PUBLIC, "transform", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
	//		mv.visitCode();
	//		mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
	//		mv.visitInsn(DUP);
	//		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
	//		mv.visitVarInsn(ASTORE, 2);
	//		mv.visitInsn(ICONST_0);
	//		mv.visitVarInsn(ISTORE, 3);
	//		Label here = new Label();
	//		mv.visitLabel(here);
	//		mv.visitVarInsn(ILOAD, 3);
	//		mv.visitVarInsn(ALOAD, 1);
	//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
	//		Label there = new Label();
	//		mv.visitJumpInsn(IF_ICMPGE, there);
	//		mv.visitVarInsn(ALOAD, 2);
	//		mv.visitVarInsn(ALOAD, 1);
	//		mv.visitVarInsn(ILOAD, 3);
	//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
	//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
	//		mv.visitInsn(POP);
	//		mv.visitIincInsn(3, 2);
	//		mv.visitJumpInsn(GOTO, here);
	//		mv.visitLabel(there);
	//		mv.visitVarInsn(ALOAD, 2);
	//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
	//		mv.visitInsn(ARETURN);
	//		mv.visitMaxs(3, 4);
	//		mv.visitEnd();
	//
	//		cw.visitEnd();
	//
	//		try {
	//			zos.write(cw.toByteArray());
	//		}
	//		catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//	}

}
