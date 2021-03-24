package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.util.Collection;
import java.util.Map;

import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import jbse.mem.Heap;
import jbse.mem.HeapObjekt;
import jbse.mem.PathCondition;

public class SymbolicHeapWithJBSE extends SymbolicHeapAsDigraph {
	
	private Heap jbseHeap;
	private PathCondition jbsePathCond;
	private Map<HeapObjekt, ObjectH> jbseObjMap;
	
	public Heap getJBSEHeap() {
		return this.jbseHeap;
	}
	
	public PathCondition getJBSEPathCond() {
		return this.jbsePathCond;
	}
	
	public Map<HeapObjekt, ObjectH> getJBSEObjMap() {
		return this.jbseObjMap;
	}
	
	public SymbolicHeapWithJBSE(Collection<ObjectH> accObjs, ExistExpr constraint,
			Heap heap, PathCondition pathCond, Map<HeapObjekt, ObjectH> objMap) {
		super(accObjs, constraint);
		this.jbseHeap = heap;
		this.jbsePathCond = pathCond;
		this.jbseObjMap = objMap;
	}

}
