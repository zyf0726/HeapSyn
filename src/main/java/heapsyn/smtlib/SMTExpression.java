package heapsyn.smtlib;

import java.util.Map;
import java.util.Set;

public interface SMTExpression {

	public SMTSort getSMTSort();
	public String toSMTString();
	public Set<Variable> getFreeVariables();
	public SMTExpression getRenaming(Map<Variable, Variable> vMap);
	
}
