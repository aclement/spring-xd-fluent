package org.springframework.xd.code;

public class Code extends CodeDrivenProcessor {

	public Code() {
		super("lambda.ser");
	}
	
	public static void main(String[] args) {
		String output = new Code().transform("foobar");
		System.out.println(output);
	}
}
