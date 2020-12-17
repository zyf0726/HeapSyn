package heapsyn.wrapper.symexec;

import java.util.Collection;
import java.util.Map;

import heapsyn.heap.ObjectH;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.Variable;

public class PathDescriptor {
	
	public SMTExpression pathCond;
	public ObjectH returnVal;
	public Collection<ObjectH> allObjs;
	public Map<ObjectH, ObjectH> objSrcMap;
	public Map<Variable, SMTExpression> varExprMap;
	
}
