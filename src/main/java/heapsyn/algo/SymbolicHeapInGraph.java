package heapsyn.algo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.BinaryExpression;
import heapsyn.smtlib.BoolVariable;
import heapsyn.smtlib.ExistentialProposition;
import heapsyn.smtlib.MultivarExpression;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;

public class SymbolicHeapInGraph extends SymbolicHeap {
	
	// only for debugging
	private static int __countHeapGenerated = 0;
	private int __heapID;
	private String __heapName;
	private Map<ObjectH, String> __objectName;
	
	private void __generateDebugInformation() {
		this.__heapID = SymbolicHeapInGraph.__countHeapGenerated;
		this.__heapName = "[H" + this.__heapID + "]";
		this.__objectName = new HashMap<>();
		int countNonNullObjs = 0;
		for (ObjectH o : this.getAllObjects()) {
			if (o.isNullObject()) {
				this.__objectName.put(o, "null");
			} else if (o.isVariable()) {
				this.__objectName.put(o, o.getVariable().toSMTString());
			} else {
				this.__objectName.put(o, "o" + countNonNullObjs + "#H" + this.__heapID);
				countNonNullObjs += 1;
			}
		}
		SymbolicHeapInGraph.__countHeapGenerated += 1;
	}
	
	public void __debugPrintOut(PrintStream ps) {
		ps.println(">> Heap " + __heapName + ": " + status + ", " + getConstraint().toSMTString());
		for (ObjectH o : getAllObjects()) {
			if (!o.isNonNullObject()) continue;
			if (isAccessible(o)) {
				ps.print("<" + __objectName.get(o) + ">");
			} else {
				ps.print(" " + __objectName.get(o) + " ");
			}
			ps.print(":");
			for (FieldH field : o.getFields())
				ps.print(" (." + field.getName() + ", " + __objectName.get(o.getValue(field)) + ")");
			ps.println();
		}
		for (BackwardRecord br : this.rcdBackwards) {
			ps.print("original heap " + br.oriHeap.__heapName + "@" + br.guardVar.toSMTString());
			if (br.mInvoke != null) {
				ps.print(", invoke " + br.mInvoke.getJavaMethod().getName() + "(");
				StringBuilder sb = new StringBuilder();
				for (ObjectH arg : br.mInvoke.getInvokeArguments()) {
					if (arg.isHeapObject()) {
						sb.append(br.oriHeap.__objectName.get(arg) + ", ");
					} else {
						sb.append(arg.getVariable().toSMTString() + ", ");
					}
				}
				sb.delete(Math.max(0, sb.length() - 2), sb.length());
				ps.print(sb.toString() + ")");
				if (br.returnVal != null) {
					ps.print(", return value is " + __objectName.get(br.returnVal));
				}
			} else {
				ps.print(", isomorphic");
			}
			ps.println();
			ps.print("   ");
			for (Entry<ObjectH, ObjectH> entry : br.objSrcMap.entrySet()) {
				if (!entry.getKey().isNonNullObject()) continue;
				ps.print(__objectName.get(entry.getKey()) + "<=");
				ps.print(br.oriHeap.__objectName.get(entry.getValue()) + ", ");
			}
			ps.println();
			ps.print("   ");
			for (Entry<Variable, SMTExpression> entry : br.varExprMap.entrySet()) {
				ps.print(entry.getKey().toSMTString() + ":=");
				ps.print(entry.getValue().toSMTString() + ", ");
			}
			ps.println();
		}
		for (ForwardRecord fr : this.rcdForwards) {
			ps.print("generate " + fr.genHeap.__heapName + " by invoking ");
			ps.print(fr.mInvoke.getJavaMethod().getName() + "(");
			StringBuilder sb = new StringBuilder();
			for (ObjectH arg : fr.mInvoke.getInvokeArguments()) {
				if (arg.isHeapObject()) {
					sb.append(__objectName.get(arg) + ", ");
				} else {
					sb.append(arg.getVariable().toSMTString() + ", ");
				}
			}
			sb.delete(Math.max(0, sb.length() - 2), sb.length());
			ps.print(sb.toString() + "), ");
			if (fr.pathCond != null) {
				ps.println("path condition is " + fr.pathCond.toSMTString());
			} else {
				ps.println("path condition is true");
			}
		}
		ps.println();
	}
	
	// the empty heap
	public static SymbolicHeapInGraph EMPTY_HEAP = new SymbolicHeapInGraph();
	
	// the minimal length of method invoke sequence
	int minSeqLen;
	
	// status while building heap transformation graph
	private HeapStatus status;
	
	// record for backtracking 
	static class BackwardRecord {
		SymbolicHeapInGraph oriHeap;
		MethodInvoke mInvoke;
		SMTExpression pathCond;
		ObjectH returnVal;
		Map<ObjectH, ObjectH> objSrcMap;
		Map<Variable, SMTExpression> varExprMap;
		BoolVariable guardVar;
	}
	private List<BackwardRecord> rcdBackwards;
	
	// record for forward traversal
	static class ForwardRecord {
		SymbolicHeapInGraph genHeap;
		MethodInvoke mInvoke;
		SMTExpression pathCond;
	}
	private List<ForwardRecord> rcdForwards;
	
	// constructor for empty heap
	private SymbolicHeapInGraph() {
		super();
		this.minSeqLen = 0;
		this.status = HeapStatus.HEAP_ACTIVE;
		this.rcdBackwards = Collections.emptyList();
		this.rcdForwards = new ArrayList<>();
		__generateDebugInformation();
	}
	
	// constructor if created by invoking a public method
	public SymbolicHeapInGraph(Collection<ObjectH> accObjs,
			SymbolicHeapInGraph oriHeap,
			MethodInvoke mInvoke,
			SMTExpression pathCond,
			ObjectH returnVal,
			Map<ObjectH, ObjectH> objSrcMap,
			Map<Variable, SMTExpression> varExprMap) {
		super(accObjs, null);
		this.minSeqLen = oriHeap.minSeqLen + 1;
		this.status = HeapStatus.HEAP_ACTIVE;
		this.rcdBackwards = new ArrayList<>();
		this.rcdForwards = new ArrayList<>();
		this.addBackwardRecord(oriHeap, mInvoke, pathCond, returnVal, objSrcMap, varExprMap);
		oriHeap.addForwardRecord(this, mInvoke, pathCond);
		__generateDebugInformation();
	}
	
	public void involveIsomorphicHeap(SymbolicHeapInGraph otherHeap, Bijection<ObjectH, ObjectH> isoMap) {
		Map<Variable, SMTExpression> varExprMap = new HashMap<>();
		for (ObjectH o : this.getAllObjects()) {
			if (o.isVariable())
				varExprMap.put(o.getVariable(), isoMap.getU(o).getVariable());
		}
		this.addBackwardRecord(otherHeap, null, null, null, isoMap.getMapV2U(), varExprMap);
	}
	
	private void addBackwardRecord(SymbolicHeapInGraph oriHeap,
			MethodInvoke mInvoke,
			SMTExpression pathCond,
			ObjectH returnVal,
			Map<ObjectH, ObjectH> objSrcMap,
			Map<Variable, SMTExpression> varExprMap) {
		BackwardRecord br = new BackwardRecord();
		br.oriHeap = oriHeap;
		br.mInvoke = mInvoke;
		br.pathCond = pathCond;
		br.returnVal = returnVal;
		br.objSrcMap = new HashMap<>(objSrcMap);
		br.varExprMap = new HashMap<>(varExprMap);
		br.guardVar = new BoolVariable();	
		this.rcdBackwards.add(br);
	}
	
	private void addForwardRecord(SymbolicHeapInGraph genHeap,
			MethodInvoke mInvoke,
			SMTExpression pathCond) {
		ForwardRecord fr = new ForwardRecord();
		fr.genHeap = genHeap;
		fr.mInvoke = mInvoke;
		fr.pathCond = pathCond;
		this.rcdForwards.add(fr);
	}
	
	public void recomputeConstraint() {
		if (this == EMPTY_HEAP) return;
		
		Set<Variable> boundVars = new HashSet<>();
		List<SMTExpression> guardConds = new ArrayList<>();
		List<SMTExpression> guardVars = new ArrayList<>(); 
		
		for (BackwardRecord br : this.rcdBackwards) {
			boundVars.addAll(br.oriHeap.getConstraint().getBoundVariables());
			boundVars.addAll(br.oriHeap.getAllVariables());
			if (br.mInvoke != null) {
				for (ObjectH arg : br.mInvoke.getInvokeArguments()) {
					if (arg.isVariable())
						boundVars.add(arg.getVariable());
				}
			}
			
			List<SMTExpression> operands = new ArrayList<>();
			operands.add(br.oriHeap.getConstraint().getBody());
			if (br.pathCond != null) {
				operands.add(br.pathCond);
			}
			for (Variable var : this.getAllVariables()) {
				operands.add(new BinaryExpression(
					SMTOperator.BINOP_EQUAL, var, br.varExprMap.get(var)
				));
			}
			
			guardConds.add(new BinaryExpression(
				SMTOperator.BINOP_EQUAL,
				br.guardVar,
				new MultivarExpression(SMTOperator.BINOP_AND, operands)
			));
			boundVars.add(br.guardVar);
			guardVars.add(br.guardVar);
		}
		guardConds.add(new MultivarExpression(SMTOperator.BINOP_OR, guardVars));
		
		this.setConstraint(new ExistentialProposition(
			boundVars,
			new MultivarExpression(SMTOperator.BINOP_AND, guardConds)
		));
	}
	
	public void makeDeprecated() {
		this.status = HeapStatus.HEAP_DEPRECATED;
	}
	
	public boolean isDeprecated() {
		return this.status == HeapStatus.HEAP_DEPRECATED;
	}
}
