package heapsyn.smtlib;

import java.util.List;

/**
 * SMT-LIB built-in operator (e.g., <tt>and</tt>, <tt>+</tt> and <tt>=></tt>) 
 * or user-defined function (by command <tt>define-fun</tt>)
 */

public interface SMTFunction {
	
	public String getName();
	
	// only for user-defined function
	public List<Variable> getArgs();
	public SMTSort getRange();
	public SMTExpression getBody();
	
}
