package heapsyn.smtlib;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Constant implements SMTExpression {

	@Override
	public Set<Variable> getFreeVariables() {
		return new HashSet<>();
	}

	@Override
	public SMTExpression getRenaming(Map<Variable, Variable> vMap) {
		return this;
	}
	
}
