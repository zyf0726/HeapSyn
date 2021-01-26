package examples.node;
/*
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import heapsyn.algo.MethodInvoke;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.HeapReprAsDigraphTest;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.SymbolicExecutor;

public class ManualExecutor implements SymbolicExecutor {
	
	public static ManualExecutor INSTANCE;
	
	public static ManualExecutor I() {
		if (INSTANCE == null)
			INSTANCE = new ManualExecutor();
		return INSTANCE;
	}
	
	@Override
	public Collection<PathDescriptor> executeMethod(HeapReprAsDigraphTest initHeap, MethodInvoke mInvoke) {
		if (mInvoke.getJavaMethod().equals(Node.mNew))
			return __execute$__new__(initHeap, mInvoke);
		else if (mInvoke.getJavaMethod().equals(Node.mGetNext))
			return __execute$getNext(initHeap, mInvoke);
		else if (mInvoke.getJavaMethod().equals(Node.mSetElem))
			return __execute$setElem(initHeap, mInvoke);
		else if (mInvoke.getJavaMethod().equals(Node.mAddAfter))
			return __execute$addAfter(initHeap, mInvoke);
		else if (mInvoke.getJavaMethod().equals(Node.mAddBefore))
			return __execute$addBefore(initHeap, mInvoke);
		else {
			// TODO
			return new ArrayList<PathDescriptor>(); 
		}
	}
	
	@SuppressWarnings("serial")
	private Collection<PathDescriptor> 
	__execute$__new__(HeapReprAsDigraphTest initHeap, MethodInvoke mInvoke) {
		Variable arg$elem = mInvoke.getInvokeArguments().get(0).getVariable();
		
		// Path 0
		PathDescriptor pd0 = new PathDescriptor();
		Bijection<ObjectH, ObjectH> cloneMap0 = new Bijection<>();
		pd0.pathCond = null;
		pd0.allObjs = initHeap.cloneObjects(cloneMap0);
		pd0.objSrcMap = cloneMap0.getMapV2U();
		pd0.varExprMap = new HashMap<>();
		for (ObjectH o : pd0.allObjs) {
			if (o.isVariable())
				pd0.varExprMap.put(o.getVariable(), pd0.objSrcMap.get(o).getVariable());
		}
		
		ObjectH varNew0 = new ObjectH(new IntVar()); 
		ObjectH objNew0 = new ObjectH(Node.classH, new HashMap<FieldH, ObjectH>() {
			{ put(Node.fElem, varNew0); put(Node.fNext, ObjectH.NULL); }
		});
		
		pd0.allObjs.addAll(Arrays.asList(objNew0, varNew0));
		pd0.varExprMap.put(varNew0.getVariable(), arg$elem);
		pd0.returnVal = objNew0;
		
		return Arrays.asList(pd0);
	}
	
	private Collection<PathDescriptor>
	__execute$getNext(HeapReprAsDigraphTest initHeap, MethodInvoke mInvoke) {
		List<PathDescriptor> pdList = new ArrayList<>();
		ObjectH arg$this = mInvoke.getInvokeArguments().get(0);
		
		if (arg$this.isNullObject())
			return pdList;
		
		// path 0
		PathDescriptor pd0 = new PathDescriptor();
		Bijection<ObjectH, ObjectH> cloneMap0 = new Bijection<>();
		pd0.pathCond = null;
		pd0.allObjs = initHeap.cloneObjects(cloneMap0);
		pd0.objSrcMap = cloneMap0.getMapV2U();
		pd0.varExprMap = new HashMap<>();
		for (ObjectH o : pd0.allObjs) {
			if (o.isVariable())
				pd0.varExprMap.put(o.getVariable(), pd0.objSrcMap.get(o).getVariable());
		}
		
		ObjectH this$next = cloneMap0.getV(arg$this).getFieldValue(Node.fNext);
		pd0.returnVal = this$next;
		
		return Arrays.asList(pd0);
	}
	
	private Collection<PathDescriptor> 
	__execute$setElem(HeapReprAsDigraphTest initHeap, MethodInvoke mInvoke) {
		List<PathDescriptor> pdList = new ArrayList<>();
		ObjectH arg$this = mInvoke.getInvokeArguments().get(0);
		Variable arg$elem = mInvoke.getInvokeArguments().get(1).getVariable();
		
		if (arg$this.isNullObject())
			return pdList;
		Variable this$elem = arg$this.getFieldValue(Node.fElem).getVariable();
		
		// Path 0
		PathDescriptor pd0 = new PathDescriptor();
		Bijection<ObjectH, ObjectH> cloneMap0 = new Bijection<>();
		pd0.pathCond = new ApplyExpr(SMTOperator.BIN_EQ, this$elem, arg$elem);
		pd0.returnVal = null;
		pd0.allObjs = initHeap.cloneObjects(cloneMap0);
		pd0.objSrcMap = cloneMap0.getMapV2U();
		pd0.varExprMap = new HashMap<>();
		for (ObjectH o : pd0.allObjs) {
			if (o.isVariable())
				pd0.varExprMap.put(o.getVariable(), pd0.objSrcMap.get(o).getVariable());
		}
		
		// Path 1
		PathDescriptor pd1 = new PathDescriptor();
		Bijection<ObjectH, ObjectH> cloneMap1 = new Bijection<>();
		pd1.pathCond = new ApplyExpr(SMTOperator.BIN_NE, this$elem, arg$elem);
		pd1.returnVal = null;
		pd1.allObjs = initHeap.cloneObjects(cloneMap1);
		pd1.objSrcMap = cloneMap1.getMapV2U();
		pd1.varExprMap = new HashMap<>();
		for (ObjectH o : pd1.allObjs) {
			if (o.isVariable())
				pd1.varExprMap.put(o.getVariable(), pd1.objSrcMap.get(o).getVariable());
		}
		pd1.varExprMap.put(cloneMap1.getV(arg$this).getFieldValue(Node.fElem).getVariable(), arg$elem);
		
		return Arrays.asList(pd0, pd1);
	}
		
	@SuppressWarnings("serial")
	private Collection<PathDescriptor> 
	__execute$addAfter(HeapReprAsDigraphTest initHeap, MethodInvoke mInvoke) {
		List<PathDescriptor> pdList = new ArrayList<>();
		ObjectH arg$this = mInvoke.getInvokeArguments().get(0);
		Variable arg$elem = mInvoke.getInvokeArguments().get(1).getVariable();
		
		if (arg$this.isNullObject())
			return pdList;
		
		// Path 0
		PathDescriptor pd0 = new PathDescriptor();
		Bijection<ObjectH, ObjectH> cloneMap0 = new Bijection<>();
		pd0.pathCond = null;
		pd0.returnVal = null;
		pd0.allObjs = initHeap.cloneObjects(cloneMap0);
		pd0.objSrcMap = cloneMap0.getMapV2U();
		pd0.varExprMap = new HashMap<>();
		for (ObjectH o : pd0.allObjs) {
			if (o.isVariable())
				pd0.varExprMap.put(o.getVariable(), pd0.objSrcMap.get(o).getVariable());
		}
		
		ObjectH varNew0 = new ObjectH(new IntVar());
		ObjectH objNew0 = new ObjectH(Node.classH, new HashMap<FieldH, ObjectH>() {
			{ put(Node.fElem, varNew0); put(Node.fNext, ObjectH.NULL); }
		});
		cloneMap0.getV(arg$this).setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(Node.fElem, cloneMap0.getV(arg$this).getFieldValue(Node.fElem)); }
			{ put(Node.fNext, objNew0); }
		});
		
		pd0.allObjs.addAll(Arrays.asList(varNew0, objNew0));
		pd0.varExprMap.put(varNew0.getVariable(), arg$elem);
		
		return Arrays.asList(pd0);
	}
	
	@SuppressWarnings("serial")
	private Collection<PathDescriptor> 
	__execute$addBefore(HeapReprAsDigraphTest initHeap, MethodInvoke mInvoke) {
		List<PathDescriptor> pdList = new ArrayList<>();
		ObjectH arg$this = mInvoke.getInvokeArguments().get(0);
		Variable arg$elem = mInvoke.getInvokeArguments().get(1).getVariable();
		
		if (arg$this.isNullObject())
			return pdList;
		
		// Path 0
		PathDescriptor pd0 = new PathDescriptor();
		Bijection<ObjectH, ObjectH> cloneMap0 = new Bijection<>();
		pd0.pathCond = null;
		pd0.allObjs = initHeap.cloneObjects(cloneMap0);
		pd0.objSrcMap = cloneMap0.getMapV2U();
		pd0.varExprMap = new HashMap<>();
		for (ObjectH o : pd0.allObjs) {
			if (o.isVariable())
				pd0.varExprMap.put(o.getVariable(), pd0.objSrcMap.get(o).getVariable());
		}
		
		ObjectH varNew0 = new ObjectH(new IntVar());
		ObjectH objNew0 = new ObjectH(Node.classH, new HashMap<FieldH, ObjectH>() {
			{ put(Node.fElem, varNew0); put(Node.fNext, cloneMap0.getV(arg$this)); }
		});
		
		pd0.allObjs.addAll(Arrays.asList(varNew0, objNew0));
		pd0.varExprMap.put(varNew0.getVariable(), arg$elem);
		pd0.returnVal = objNew0;
		
		return Arrays.asList(pd0);
	}

	@Override
	public Collection<PathDescriptor> executeMethodUnderTest(HeapReprAsDigraphTest heap, MethodInvoke mUnderTest) {
		// TODO Auto-generated method stub
		return null;
	}
	

}

*/