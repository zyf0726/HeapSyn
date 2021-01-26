/**
 * 
 */
package heapsyn.smtlib;

import java.util.Set;

/**
 * SMT-LIB quantified expression
 */

public interface SMTQuantifiedExpr {

	public String toSMTString();
	public Set<Variable> getBoundVariables();
	public SMTExpression getBody();
	
}
