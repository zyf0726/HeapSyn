package heapsyn.wrapper.smt;

import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.SMTExpression;

public interface IncrSMTSolver extends SMTSolver {
	
	public void initIncrSolver();
	
	public void pushAssert(SMTExpression p);
	
	public void pushAssertNot(ExistExpr p);
	
	public void endPushAssert();
	
	public boolean checkSatIncr(SMTExpression p);
	
	public void closeIncrSolver();
	
}
