package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.util.Collection;
import java.util.Map;

import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import jbse.mem.HeapObjekt;
import jbse.mem.State;
import jbse.val.Primitive;

public class SymbolicHeapWithJBSE extends SymbolicHeapAsDigraph {

//	private Heap jbseHeap;
//	private PathCondition jbsePathCond;
	private State jbseState;
	private Map<HeapObjekt, ObjectH> jbseObjMap;
	private Map<Primitive,ObjectH> jbseVarMap;

//	public Heap getJBSEHeap() {
//		return this.jbseHeap;
//	}
//
//	public PathCondition getJBSEPathCond() {
//		return this.jbsePathCond;
//	}
	
	public State getJBSEState() {
		return this.jbseState;
	}

	public Map<HeapObjekt, ObjectH> getJBSEObjMap() {
		return this.jbseObjMap;
	}
	
	public Map<Primitive,ObjectH> getJBSEVarMap() {
		return this.jbseVarMap;
	}

	public SymbolicHeapWithJBSE(Collection<ObjectH> accObjs, ExistExpr constraint, State state,
			Map<HeapObjekt, ObjectH> objMap,Map<Primitive,ObjectH> varMap) {
		super(accObjs, constraint);
//		this.jbseHeap = heap;
//		this.jbsePathCond = pathCond;
		this.jbseState = state;
		this.jbseObjMap = objMap;
		this.jbseVarMap = varMap;
	}

}
