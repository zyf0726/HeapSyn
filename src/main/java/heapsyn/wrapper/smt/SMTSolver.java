package heapsyn.wrapper.smt;

import java.util.Map;

import heapsyn.smtlib.Constant;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.Variable;

public interface SMTSolver {
	
	public boolean checkSat(SMTExpression constraint, Map<Variable, Constant> model);

}
