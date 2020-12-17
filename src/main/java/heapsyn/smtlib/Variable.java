package heapsyn.smtlib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Variable implements SMTExpression {
	
	private String varName;
	
	Variable(String varName) {
		if (varName == null)
			throw new IllegalArgumentException("a non-null variable name expected");
		this.varName = varName;
	}
	
	public abstract Variable cloneVariable();
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Variable))
			return false;
		return this.varName.equals(((Variable) obj).varName);
	}
	
	@Override
	public int hashCode() {
		return this.varName.hashCode();
	}
	
	@Override
	public String toSMTString() {
		return this.varName;
	}
	
	@Override
	public Set<Variable> getFreeVariables() {
		return new HashSet<>(Collections.singleton(this));
	}
	
	@Override
	public SMTExpression getRenaming(Map<Variable, Variable> vMap) {
		if (vMap.containsKey(this))
			return vMap.get(this);
		return this;
	}
}
