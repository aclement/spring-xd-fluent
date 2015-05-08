package org.springframework.xd.code;

public class Code extends CodeDrivenProcessor {

	public Code() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Integer transform(Integer input) {
		return (Integer)fn.apply(input);
	}
	
}
