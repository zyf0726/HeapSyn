package heapsyn.smtlib;

public class BoolConst extends Constant {
	
	public static BoolConst DEFAULT = new BoolConst(false);
	
	private boolean aBool; 

	public BoolConst(boolean aBool) {
		this.aBool = aBool;
	}
	
	@Override
	public SMTSort getSMTSort() {
		return SMTSort.BOOL;
	}
	
	@Override
	public String toSMTString() {
		return String.valueOf(this.aBool);
	}

}
