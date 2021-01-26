package heapsyn.wrapper.symbolic;

import java.util.Map;

import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.Variable;

public class PathDescriptor {
	
	public SMTExpression pathCond;
	public ObjectH retVal;
	public SymbolicHeap finHeap;
	
	public Map<ObjectH, ObjectH> objSrcMap;
	public Map<Variable, SMTExpression> varExprMap;
	
}
