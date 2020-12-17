package heapsyn.smtlib;

public class IntConstant extends Constant {
	
	private long aInt;
	
	public IntConstant(long aInt) {
		this.aInt = aInt;
	}

	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_INT;
	}

	@Override
	public String toSMTString() {
		return String.valueOf(this.aInt);
	}

}
