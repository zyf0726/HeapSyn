package heapsyn.smtlib;

public class IntVariable extends Variable {
	
	static final String VARNAME_PREFIX = "I";
	
	private static int countVars = 0;
	
	public static int getCounter() {
		return IntVariable.countVars;
	}
	
	public IntVariable() {
		super(VARNAME_PREFIX + String.valueOf(IntVariable.countVars));
		IntVariable.countVars += 1;
	}
	
	IntVariable(String varName) {
		super(varName);
		IntVariable.countVars += 1;
	}
	
	@Override
	public Variable cloneVariable() {
		return new IntVariable();
	}
	
	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_INT;
	}
	
}
