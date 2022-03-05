package heapsyn.wrapper.smt;

import java.util.Map;

import heapsyn.smtlib.Constant;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.Variable;

public interface SMTSolver {
	
	public boolean checkSat(SMTExpression constraint, Map<Variable, Constant> model);
	
	public boolean checkSat$pAndNotq(SMTExpression p, ExistExpr q);
	
}
