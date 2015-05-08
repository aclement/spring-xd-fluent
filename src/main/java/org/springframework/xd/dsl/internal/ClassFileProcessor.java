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

package org.springframework.xd.dsl.internal;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;


/**
 *
 * @author aclement
 */
@SuppressWarnings("unused")
public class ClassFileProcessor {

	private String typename;

	ClassFileProcessor(String dottedTypeName) {
		this.typename = dottedTypeName;
	}

	private ClassMetaData process() {
		String resourceName = this.typename.replaceAll("\\.", "/") + ".class";
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				resourceName)) {
			if (is == null) {
				throw new IllegalStateException("Unable to find resource: " + resourceName);
			}
			ClassReader cr = new ClassReader(is);
			ClassMetaData cmd = new ClassMetaData();
			cr.accept(cmd, 0);
			return cmd;
			//			return read(is);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private ClassMetaData read(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		int magic = dis.readInt(); // jump over magic 0xCAFEBABE
		if (magic != 0xCAFEBABE) {
			throw new IllegalStateException("incorrect magic number 0x" + Integer.toHexString(magic).toUpperCase());
		}
		dis.skip(2); // minor version
		dis.skip(2); // major version
		int cpcount = dis.readShort();
		List<ConstantPoolEntry> cpEntries = new ArrayList<>();
		System.out.println(cpcount);
		for (int i = 0; i < (cpcount - 1); i++) {
			cpEntries.add(readConstantPoolEntry(dis));
		}
		return new ClassMetaData(cpEntries);
	}

	public static class ClassMetaData extends ClassVisitor implements Opcodes {

		private final static boolean debug = true;

		private List<ConstantPoolEntry> cpEntries;

		private List<String> typeRefs;

		public ClassMetaData(List<ConstantPoolEntry> cpEntries) {
			super(ASM5);
			this.cpEntries = cpEntries;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitAttribute(Attribute attr) {
			super.visitAttribute(attr);
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			if (debug)
				System.out.println("visitField(name=" + name + ")");
			return super.visitField(access, name, desc, signature, value);
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			super.visitInnerClass(name, outerName, innerName, access);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			//			if (name.startsWith("lambda$")) {
			return new MethodMetaData(access, name, desc, signature, exceptions);
			//			}
			//			else {
			//				return null;
			//				// return super.visitMethod(access, name, desc, signature, exceptions);
			//			}
		}

		class MethodMetaData extends MethodVisitor {

			public MethodMetaData(int access, String name, String desc, String signature, String[] exceptions) {
				super(ASM5, null);
				if (debug)
					System.out.println("MethodMetaData(access=0x" + Integer.toHexString(access).toUpperCase()
							+ ",name="
							+ name + ",desc=" + desc
							+ ",signature=" + signature + ")");
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				if (debug)
					System.out.println("visitMethodInsn: " + owner);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				if (debug)
					System.out.println("visitMethodInsn(opcode=" + opcode + ",owner=" + owner + ",name=" + name
							+ ",desc="
							+ desc + ",itf=" + itf + ")");
				addTypeReference(owner);
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				if (debug)
					System.out.println("visitTypeInsn:" + type);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				if (debug)
					System.out.println("visitFieldInsn(opcode=" + opcode + ",owner=" + owner + ",name=" + name
							+ ",desc="
							+ desc + ")");
			}

		}

		@Override
		public void visitOuterClass(String owner, String name, String desc) {
			super.visitOuterClass(owner, name, desc);
		}

		@Override
		public void visitSource(String source, String debug) {
			super.visitSource(source, debug);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
		}

		public ClassMetaData() {
			super(ASM5);
		}

		void addTypeReference(String typeSignature) {
			if (typeSignature.startsWith("java/")) {
				return;
			}
			if (this.typeRefs == null) {
				this.typeRefs = new ArrayList<>();
			}
			this.typeRefs.add(typeSignature);
		}

		/**
		 * Return the type references in slashed form (e.g. com/foo/Bar)
		 *
		 */
		public List<String> getTypeReferences() {
			return this.typeRefs;
		}

	}

	interface ConstantPoolEntry {

		int getTag();
	}

	class ClassInfo implements ConstantPoolEntry {

		private int nameIndex;

		public ClassInfo(int nameIndex) {
			System.out.println("ClassInfo " + nameIndex);
			this.nameIndex = nameIndex;
		}

		public String getTypeReference(List<ConstantPoolEntry> cp) {
			return ((Utf8Info) cp.get(nameIndex - 1)).getString();
		}

		@Override
		public int getTag() {
			return 7;
		}

	}

	class Utf8Info implements ConstantPoolEntry {

		private String utf8;

		public Utf8Info(String utf8) {
			System.out.println("Utf8Info " + utf8);
			this.utf8 = utf8;
		}

		public String getString() {
			return this.utf8;
		}

		@Override
		public int getTag() {
			return 1;
		}

	}

	class StringInfo implements ConstantPoolEntry {

		private int stringIndex;

		public StringInfo(int stringIndex) {
			this.stringIndex = stringIndex;
		}

		@Override
		public int getTag() {
			return 99;
		}

	}

	class MethodRefInfo implements ConstantPoolEntry {

		private int classIndex;

		private int nameAndTypeIndex;

		public MethodRefInfo(int classIndex, int nameAndTypeIndex) {
			System.out.println("MethodRefInfo: " + classIndex + "," + nameAndTypeIndex);
			this.classIndex = classIndex;
			this.nameAndTypeIndex = nameAndTypeIndex;
		}

		@Override
		public int getTag() {
			return 10;
		}

	}

	class NameAndTypeInfo implements ConstantPoolEntry {

		private int nameIndex;

		private int descriptorIndex;

		public NameAndTypeInfo(int nameIndex, int descriptorIndex) {
			System.out.println("NameAndTypeInfo: " + nameIndex + "," + descriptorIndex);
			this.nameIndex = nameIndex;
			this.descriptorIndex = descriptorIndex;
		}

		@Override
		public int getTag() {
			return 12;
		}

	}

	private ConstantPoolEntry readConstantPoolEntry(DataInputStream is) throws IOException {
		byte tag = is.readByte();
		switch (tag) {
			case 7:// CONSTANT_Class 7
				return new ClassInfo(is.readShort());
			case 1:// CONSTANT_Utf8 1
				return new Utf8Info(is.readUTF());
			case 10:// CONSTANT_Methodref 10
				return new MethodRefInfo(is.readShort(), is.readShort());
			case 12: //			CONSTANT_NameAndType	12
				return new NameAndTypeInfo(is.readShort(), is.readShort());
			case 9://			CONSTANT_Fieldref	9
			case 11: //			CONSTANT_InterfaceMethodref	11
			case 8: //			CONSTANT_String	8
			case 3: //			CONSTANT_Integer	3
			case 4: //			CONSTANT_Float	4
			case 5: //			CONSTANT_Long	5
			case 6: //			CONSTANT_Double	6
			case 15: //			CONSTANT_MethodHandle	15
			case 16: //			CONSTANT_MethodType	16
			case 18: //			CONSTANT_InvokeDynamic	18
			default:
				throw new IllegalStateException("unexpected constant pool tag: " + tag);
		}
	}

	public static ClassMetaData getInfo(String dottedTypeName) {
		return new ClassFileProcessor(dottedTypeName).process();
	}

}
