package heapsyn.smtlib;

public class BoolVariable extends Variable {
	
	static final String VARNAME_PREFIX = "B";
	
	private static int countVars = 0;
	
	public static int getCounter() {
		return BoolVariable.countVars;
	}
	
	public BoolVariable() {
		super(VARNAME_PREFIX + String.valueOf(BoolVariable.countVars));
		BoolVariable.countVars += 1;
	}
	
	BoolVariable(String varName) {
		super(varName);
		BoolVariable.countVars += 1;
	}
	
	@Override
	public Variable cloneVariable() {
		return new BoolVariable();
	}

	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_BOOL;
	}
}
