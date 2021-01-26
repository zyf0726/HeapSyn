package heapsyn.smtlib;

public class BoolVar extends Variable {
	
	static final String VARNAME_PREFIX = "B";
	
	private static int countVars = 0;
	
	public static void resetCounter() {
		BoolVar.countVars = 0;
	}
	
	public static int getCounter() {
		return BoolVar.countVars;
	}
	
	BoolVar(String varName) {
		super(varName);
		BoolVar.countVars += 1;
	}
	
	public BoolVar() {
		this(VARNAME_PREFIX + BoolVar.countVars);
	}
	
	@Override
	public SMTSort getSMTSort() {
		return SMTSort.BOOL;
	}
}
