package heapsyn.wrapper.smt;

import java.util.Map;

import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.Variable;

public class ExternalSolver implements SMTSolver {

	@Override
	public boolean checkSat(SMTExpression constraint, Map<Variable, SMTExpression> model) {
		// TODO Auto-generated method stub
		return false;
	}

}
