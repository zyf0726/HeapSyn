package heapsyn.smtlib;

import java.util.Map;
import java.util.Set;

public class UnaryExpression implements SMTExpression {

	private SMTOperator operator;
	private SMTExpression operand;
	
	public UnaryExpression(SMTOperator operator, SMTExpression operand) {
		if (operand == null || operand == null)
			throw new IllegalArgumentException("non-null operator and operand expected");
		this.operator = operator;
		this.operand = operand;
	}

	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_UNKNOWN;
	}

	@Override
	public String toSMTString() {
		return "(" + this.operator.toSMTString() + 
			   " " + this.operand.toSMTString() + ")";
	}

	@Override
	public Set<Variable> getFreeVariables() {
		return this.operand.getFreeVariables();
	}
	
	public SMTExpression getRenaming(Map<Variable, Variable> vMap) {
		return new UnaryExpression(this.operator, this.operand.getRenaming(vMap));
	};

}
