package heapsyn.wrapper.symbolic;

import java.util.ArrayList;

/**
 * @author Zhu Ruidong
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import jbse.mem.Clause;
import jbse.mem.HeapObjekt;
import jbse.val.Primitive;

public class SymbolicHeapWithJBSE extends SymbolicHeapAsDigraph {

	//private State jbseState;
	private TreeMap<Long,HeapObjekt> objects;
	private ArrayList<Clause> clauses;
	private int refid;
	private int primid;
	private Map<HeapObjekt, ObjectH> jbseObjMap;
	private Map<Primitive,ObjectH> jbseVarMap;

	public TreeMap<Long,HeapObjekt> getObjects() {
		return this.objects;
	}

	public ArrayList<Clause> getClauses() {
		return this.clauses;
	}
	
	public int getPrimid() {
		return this.primid;
	}
	
	public int getRefid() {
		return this.refid;
	}

	public Map<HeapObjekt, ObjectH> getJBSEObjMap() {
		return this.jbseObjMap;
	}
	
	public Map<Primitive,ObjectH> getJBSEVarMap() {
		return this.jbseVarMap;
	}
	
//	public void clear() {
//		this.jbseState=null;
//		this.jbseObjMap=null;
//		this.jbseVarMap=null;
//	}
	
	public SymbolicHeapWithJBSE(ExistExpr constraint) {
		super(constraint);
		//this.jbseState=null;
		this.objects=new TreeMap<>();
		this.clauses=new ArrayList<>();
		this.primid=0;
		this.refid=0;
		this.jbseObjMap=new HashMap<>();
		this.jbseVarMap=new HashMap<>();
	}

	public SymbolicHeapWithJBSE(Collection<ObjectH> accObjs, ExistExpr constraint, TreeMap<Long,HeapObjekt> objects,
			ArrayList<Clause> clauses,int prim,int ref,Map<HeapObjekt, ObjectH> objMap,Map<Primitive,ObjectH> varMap) {
		super(accObjs, constraint);
//		this.jbseHeap = heap;
//		this.jbsePathCond = pathCond;
		//this.jbseState = state;
		this.objects=new TreeMap<>(objects);
		this.clauses=new ArrayList<>(clauses);
		this.primid=prim;
		this.refid=ref;
		this.jbseObjMap = objMap;
		this.jbseVarMap = varMap;
	}

}
