package heapsyn.wrapper.smt;

import java.util.Map;

import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.Variable;

public class ExternalSolver implements SMTSolver {

	@Override
	public boolean checkSat(ExistExpr constraint, Map<Variable, SMTExpression> model) {
		// TODO Auto-generated method stub
		return false;
	}

}
