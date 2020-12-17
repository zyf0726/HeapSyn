package heapsyn.smtlib;

import java.util.Map;
import java.util.Set;

public class BinaryExpression implements SMTExpression {
	
	private SMTOperator op;
	private SMTExpression fstOpd;
	private SMTExpression sndOpd;
	
	public BinaryExpression(SMTOperator op, SMTExpression fstOpd, SMTExpression sndOpd) {
		if (op == null || fstOpd == null || sndOpd == null)
			throw new IllegalArgumentException("a non-null operator and two non-null operands expected");
		this.op = op;
		this.fstOpd = fstOpd;
		this.sndOpd = sndOpd;
	}
	
	@Override
	public String toSMTString() {
		return "(" + this.op.toSMTString() +
			   " " + this.fstOpd.toSMTString() +
			   " " + this.sndOpd.toSMTString() + ")";
	}
	
	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_UNKNOWN;
	}
	
	@Override
	public Set<Variable> getFreeVariables() {
		Set<Variable> FVs = this.fstOpd.getFreeVariables();
		FVs.addAll(this.sndOpd.getFreeVariables());
		return FVs;
	}
	
	@Override
	public SMTExpression getRenaming(Map<Variable, Variable> vMap) {
		return new BinaryExpression(
			this.op,
			this.fstOpd.getRenaming(vMap),
			this.sndOpd.getRenaming(vMap)
		);
	}
	
}
