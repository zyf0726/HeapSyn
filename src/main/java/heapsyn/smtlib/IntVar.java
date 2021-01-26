package heapsyn.smtlib;

public class IntVar extends Variable {
	
	static final String VARNAME_PREFIX = "I";
	
	private static int countVars = 0;
	
	public static void resetCounter() {
		IntVar.countVars = 0;
	}
	
	public static int getCounter() {
		return IntVar.countVars;
	}
	
	IntVar(String varName) {
		super(varName);
		IntVar.countVars += 1;
	}
	
	public IntVar() {
		this(VARNAME_PREFIX + IntVar.countVars);
	}
	
	@Override
	public SMTSort getSMTSort() {
		return SMTSort.INT;
	}
	
}
