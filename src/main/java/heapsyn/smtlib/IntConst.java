package heapsyn.smtlib;

public class IntConst extends Constant {
	
	public static IntConst DEFAULT = new IntConst(0);
	
	private long aInt;
	
	public IntConst(long aInt) {
		this.aInt = aInt;
	}

	@Override
	public SMTSort getSMTSort() {
		return SMTSort.INT;
	}

	@Override
	public String toSMTString() {
		return String.valueOf(this.aInt);
	}

}
