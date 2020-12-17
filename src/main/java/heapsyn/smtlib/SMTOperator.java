package heapsyn.smtlib;

public enum SMTOperator {
	BINOP_ADD("+"),
	BINOP_SUB("-"),
	BINOP_MUL("*"),
	BINOP_EQUAL("="),
	BINOP_NOT_EQUAL("distinct"),
	BINOP_AND("and"),
	BINOP_OR("or"),
	BINOP_IMPLY("=>"),
	UNOP_NOT("not"),
	UNOP_NEG("-"),
	;
	
	private String repr;
	
	private SMTOperator(String repr) {
		this.repr = repr;
	}
	
	public String toSMTString() {
		return this.repr;
	}

}
