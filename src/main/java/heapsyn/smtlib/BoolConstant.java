package heapsyn.smtlib;

public class BoolConstant extends Constant {
	
	private boolean aBool; 

	public BoolConstant(boolean aBool) {
		this.aBool = aBool;
	}
	
	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_BOOL;
	}
	
	@Override
	public String toSMTString() {
		return String.valueOf(this.aBool);
	}

}
