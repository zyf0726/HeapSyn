package heapsyn.smtlib;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExistentialProposition implements SMTExpression {
	
	public static ExistentialProposition ALWAYS_TRUE =
			new ExistentialProposition(null, new BoolConstant(true));
	
	private Set<Variable> boundVars;
	private SMTExpression body;
	
	public ExistentialProposition(Collection<Variable> boundVars, SMTExpression body) {
		if (body == null)
			throw new IllegalArgumentException("a non-null proposition expected");
		
		if (boundVars == null) {
			this.boundVars = new HashSet<>();
		} else {
			this.boundVars = new HashSet<>(boundVars);
		}
		this.body = body;
	}

	@Override
	public SMTSort getSMTSort() {
		return SMTSort.SMT_BOOL;
	}

	@Override
	public String toSMTString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("(exists (");
		for (Variable var : this.boundVars) {
			sb.append("(" + var.toSMTString() + " " + var.getSMTSort().toSMTString() + ") ");
		}
		if (this.boundVars.isEmpty()) {
			sb.append("(DUMMY_VAR " + SMTSort.SMT_BOOL.toSMTString() + ") ");
		}
		sb.append(") " + this.body.toSMTString() + ")");
		return sb.toString();
	}

	@Override
	public Set<Variable> getFreeVariables() {
		Set<Variable> FVs = this.body.getFreeVariables();
		FVs.removeAll(this.boundVars);
		return FVs;
	}
	
	public Set<Variable> getBoundVariables() {
		if (this.body instanceof ExistentialProposition) {
			Set<Variable> BVs = ((ExistentialProposition) this.body).getBoundVariables();
			BVs.addAll(this.boundVars);
			return BVs;
		} else {
			return new HashSet<>(this.boundVars);
		}
	}
	
	public SMTExpression getBody() {
		if (this.body instanceof ExistentialProposition) {
			return ((ExistentialProposition) this.body).getBody();
		} else {
			return this.body;
		}
	}
	
	@Override
	public SMTExpression getRenaming(Map<Variable, Variable> vMap) {
		for (Variable v : this.boundVars)
			assert(!vMap.containsKey(v));
		return new ExistentialProposition(this.boundVars, this.body.getRenaming(vMap));
	}

}
