package heapsyn.smtlib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultivarExpression implements SMTExpression {
	
	private SMTOperator operator;
	private ArrayList<SMTExpression> operands;
	
	public MultivarExpression(SMTOperator op, List<SMTExpression> opds) {
		if (op == null || opds == null)
			throw new IllegalArgumentException("non-null operator and operand list expected");
		this.operator = op;
		this.operands = new ArrayList<>(opds);
	}

	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_UNKNOWN;
	}

	@Override
	public String toSMTString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(" + this.operator.toSMTString());
		for (SMTExpression operand : this.operands) {
			sb.append(" " + operand.toSMTString());
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Set<Variable> getFreeVariables() {
		Set<Variable> FVs = new HashSet<>();
		for (SMTExpression operand : this.operands) {
			FVs.addAll(operand.getFreeVariables());
		}
		return FVs;
	}

	@Override
	public SMTExpression getRenaming(Map<Variable, Variable> vMap) {
		List<SMTExpression> renamedOperands = new ArrayList<>();
		for (SMTExpression operand : this.operands) {
			renamedOperands.add(operand.getRenaming(vMap));
		}
		return new MultivarExpression(this.operator, renamedOperands);
	}

}
