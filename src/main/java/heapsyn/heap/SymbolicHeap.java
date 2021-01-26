package heapsyn.heap;

import java.util.List;
import java.util.Set;

import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;

public interface SymbolicHeap {
	
	public Set<ObjectH> getAllObjects();
	public Set<ObjectH> getAccessibleObjects();
	public List<Variable> getVariables();
	public ExistExpr getConstraint();
	
	public void setConstraint(ExistExpr constraint);
	
	public long getFeatureCode();
	public boolean maybeIsomorphicWith(SymbolicHeap heap);
	public boolean surelySubsumedBy(SymbolicHeap heap);
	public boolean findIsomorphicMappingTo(SymbolicHeap heap, ActionIfFound action);
	public boolean findEmbeddingInto(SymbolicHeap heap, ActionIfFound action);
	
	public Set<ObjectH> cloneAllObjects(Bijection<ObjectH, ObjectH> cloneMap);

}
