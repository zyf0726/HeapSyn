package heapsyn.algo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import heapsyn.common.settings.Options;
import heapsyn.heap.ActionIfFound;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.SMTSort;
import heapsyn.smtlib.UserFunc;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;
import heapsyn.wrapper.smt.SMTSolver;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.Specification;

public class WrappedHeap {
	
	// only for debugging
	private static int __countHeapGenerated = 0;
	private int __heapID;
	private String __heapName;
	private Map<ObjectH, String> __objNameMap;
	private List<UserFunc> __funCreated;
	
	private void __generateDebugInformation() {
		this.__heapID = WrappedHeap.__countHeapGenerated++;
		this.__heapName = "[H" + this.__heapID + "]";
		this.__objNameMap = new HashMap<>();
		this.__funCreated = new ArrayList<>();
		int countNonNullObjs = 0;
		for (ObjectH o : this.heap.getAllObjects()) {
			if (o.isNullObject()) {
				this.__objNameMap.put(o, "null");
			} else if (o.isVariable()) {
				this.__objNameMap.put(o, o.getVariable().toSMTString());
			} else {
				this.__objNameMap.put(o, "o" + (countNonNullObjs++) + "#H" + this.__heapID);
			}
		}
	}
	
	public void __debugPrintOut(PrintStream ps) {
		ps.print(">> Heap " + __heapName + ": " + status + ", ");
		if (heap.getConstraint() != null) {
			ps.println(heap.getConstraint().toSMTString());
		} else {
			ps.println("constraint undetermined");
		}
		for (UserFunc uf : __funCreated) {
			ps.println("   " + uf.getSMTDef());
		}
		for (ObjectH o : heap.getAllObjects()) {
			if (!o.isNonNullObject()) continue;
			if (heap.getAccessibleObjects().contains(o)) {
				ps.print("<" + __objNameMap.get(o) + ">");
			} else {
				ps.print(" " + __objNameMap.get(o) + " ");
			}
			ps.print(":");
			for (FieldH field : o.getFields())
				ps.print(" (." + field.getName() + ", " + __objNameMap.get(o.getFieldValue(field)) + ")");
			ps.println();
		}
		if (heap.getAccessibleObjects().stream().anyMatch(o -> o.isVariable())) {
			for (ObjectH o : heap.getAccessibleObjects()) {
				if (o.isVariable())
					ps.print("<" + __objNameMap.get(o) + "> ");
			}
			ps.println();
		}
		if (!renameMapList.isEmpty()) {
			ps.println("variable renaming map list:");
			for (Map<Variable, Variable> renameMap : renameMapList) {
				ps.print("   { ");
				for (Entry<Variable, Variable> entry : renameMap.entrySet()) {
					ps.print(entry.getKey().toSMTString() + "=>");
					ps.print(entry.getValue().toSMTString() + ", ");
				}
				ps.println("}");
			}
		}
		for (BackwardRecord br : rcdBackwards) {
			ps.print("original heap " + br.oriHeap.__heapName);
			if (br.mInvoke != null) {
				ps.print(", invoke " + br.mInvoke.getJavaMethod().getName() + "(");
				StringBuilder sb = new StringBuilder();
				for (ObjectH arg : br.mInvoke.getInvokeArguments()) {
					if (arg.isHeapObject()) {
						sb.append(br.oriHeap.__objNameMap.get(arg) + ", ");
					} else {
						sb.append(arg.getVariable().toSMTString() + ", ");
					}
				}
				sb.delete(Math.max(0,  sb.length() - 2), sb.length());
				ps.print(sb.toString() + ")");
				if (br.retVal != null) {
					ps.print(", return value is " + __objNameMap.get(br.retVal));
				}
			} else {
				ps.print(", isomorphic");
			}
			ps.println();
			if (!br.objSrcMap.isEmpty()) {
				ps.print("   ");
				for (Entry<ObjectH, ObjectH> entry : br.objSrcMap.entrySet()) {
					if (entry.getKey().isNonNullObject()) {
						ps.print(__objNameMap.get(entry.getKey()) + "<=");
						ps.print(br.oriHeap.__objNameMap.get(entry.getValue()) + ", ");
					}
				}
				ps.println();
			}
			if (!br.varExprMap.isEmpty()) {
				ps.print("   ");
				for (Entry<Variable, SMTExpression> entry : br.varExprMap.entrySet()) {
					ps.print(entry.getKey().toSMTString() + ":=");
					ps.print(entry.getValue().toSMTString() + ", ");
				}
				ps.println();
			}
			if (!br.guardCondList.isEmpty()) {
				for (ExistExpr guardCond : br.guardCondList) {
					ps.println("   " + guardCond.toSMTString());
				}
			} else {
				ps.println("   guard condition undetermined");
			}
		}
		for (ForwardRecord fr : rcdForwards) {
			ps.print("generate " + fr.finHeap.__heapName + " by invoking ");
			ps.print(fr.mInvoke.getJavaMethod().getName() + "(");
			StringBuilder sb = new StringBuilder();
			for (ObjectH arg : fr.mInvoke.getInvokeArguments()) {
				if (arg.isHeapObject()) {
					sb.append(__objNameMap.get(arg) + ", ");
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
	
	
	// symbolic heap
	private SymbolicHeap heap;
	
	// status while building heap transformation graph
	private HeapStatus status;
	
	// record for backtracking
	static class BackwardRecord {
		WrappedHeap oriHeap;
		MethodInvoke mInvoke;
		SMTExpression pathCond;
		ObjectH retVal;
		Map<ObjectH, ObjectH> objSrcMap;
		Map<Variable, SMTExpression> varExprMap;
		ArrayList<ExistExpr> guardCondList;
	}
	
	private ArrayList<BackwardRecord> rcdBackwards;
	private ArrayList<Map<Variable, Variable>> renameMapList;
	
	private void addBackwardRecord(WrappedHeap oriHeap, MethodInvoke mInvoke,
			SMTExpression pathCond, ObjectH retVal,
			Map<ObjectH, ObjectH> objSrcMap,
			Map<Variable, SMTExpression> varExprMap) {
		BackwardRecord br = new BackwardRecord();
		br.oriHeap = oriHeap;
		br.mInvoke = mInvoke;
		br.pathCond = pathCond;
		br.retVal = retVal;
		br.objSrcMap = ImmutableMap.copyOf(objSrcMap);
		br.varExprMap = ImmutableMap.copyOf(varExprMap);
		br.guardCondList = new ArrayList<>();
		this.rcdBackwards.add(br);
	}

	
	// record for forward traversal
	static class ForwardRecord {
		WrappedHeap finHeap;
		MethodInvoke mInvoke;
		SMTExpression pathCond;
	}
	
	private ArrayList<ForwardRecord> rcdForwards;
	
	private void addForwardRecord(WrappedHeap finHeap,	MethodInvoke mInvoke,
			SMTExpression pathCond) {
		ForwardRecord fr = new ForwardRecord();
		fr.finHeap = finHeap;
		fr.mInvoke = mInvoke;
		fr.pathCond = pathCond;
		this.rcdForwards.add(fr);
	}
	
	
	// constructor for initial heap
	public WrappedHeap(SymbolicHeap initHeap) {
		this.heap = initHeap;
		this.status = HeapStatus.ACTIVE;
		this.rcdBackwards = new ArrayList<>();
		this.renameMapList = new ArrayList<>();
		this.rcdForwards = new ArrayList<>();
		__generateDebugInformation();
	}
	
	// constructed by invoking a public method
	public WrappedHeap(WrappedHeap oriHeap, MethodInvoke mInvoke, PathDescriptor pd) {
		this(pd.finHeap);
		this.addBackwardRecord(oriHeap, mInvoke, pd.pathCond, pd.retVal,
				pd.objSrcMap, pd.varExprMap);
		oriHeap.addForwardRecord(this, mInvoke, pd.pathCond);
	}
	
	// subsume an isomorphic heap
	public void subsumeHeap(WrappedHeap otherHeap, Bijection<ObjectH, ObjectH> isoMap) {
		Preconditions.checkArgument(isoMap.getMapU2V().keySet()
				.equals(otherHeap.heap.getAllObjects()));
		Preconditions.checkArgument(isoMap.getMapV2U().keySet()
				.equals(this.heap.getAllObjects()));
		
		Map<Variable, SMTExpression> varExprMap = new HashMap<>();
		for (ObjectH o : this.heap.getAllObjects()) {
			if (o.isVariable())
				varExprMap.put(o.getVariable(), isoMap.getU(o).getVariable());
		}
		this.addBackwardRecord(otherHeap, null, null, null, isoMap.getMapV2U(), varExprMap);
		otherHeap.status = HeapStatus.SUBSUMED;
	}

	// recompute the constraint of this heap
	public void recomputeConstraint() {
		if (this.rcdBackwards.isEmpty())
			return;
		
		for (BackwardRecord br : this.rcdBackwards) {
			List<SMTExpression> andClauses = new ArrayList<>();
			List<Variable> boundVars = new ArrayList<>();
			boundVars.addAll(br.oriHeap.heap.getConstraint().getBoundVariables());
			andClauses.add(br.oriHeap.heap.getConstraint().getBody());
			boundVars.addAll(br.oriHeap.heap.getVariables());
			if (br.mInvoke != null) {
				boundVars.addAll(br.mInvoke.getInvokeArguments().stream()
						.filter(o -> o.isVariable())
						.map(o -> o.getVariable())
						.collect(Collectors.toList()));
			}
			if (br.pathCond != null) {
				andClauses.add(br.pathCond);
			}
			for (Variable var : this.heap.getVariables()) {
				if (br.varExprMap.containsKey(var)) {
					SMTExpression clause = new ApplyExpr(SMTOperator.BIN_EQ,
							var, br.varExprMap.get(var));
					andClauses.add(clause);
				}
			}
			ExistExpr guardCond = new ExistExpr(boundVars,
					new ApplyExpr(SMTOperator.AND, andClauses));
			br.guardCondList.add(guardCond);
			assert(Sets.difference(
					guardCond.getBody().getFreeVariables(),
					guardCond.getBoundVariables())
					.immutableCopy().equals(ImmutableSet.copyOf(this.heap.getVariables())));
		}
		
		Map<Variable, Variable> newRenameMap = new HashMap<>();
		List<ExistExpr> orClauses = this.rcdBackwards.stream()
				.map(o -> o.guardCondList.get(o.guardCondList.size() - 1))
				.collect(Collectors.toList());
		ExistExpr orExpr = ExistExpr.makeOr(orClauses, newRenameMap);
		this.renameMapList.add(newRenameMap);
		
		List<Variable> funcArgs = new ArrayList<>(this.heap.getVariables());
		funcArgs.addAll(orExpr.getBoundVariables());
		UserFunc func = new UserFunc(funcArgs, SMTSort.BOOL, orExpr.getBody());
		this.__funCreated.add(func);
		ExistExpr constraint = new ExistExpr(orExpr.getBoundVariables(),
				new ApplyExpr(func, funcArgs));
		this.heap.setConstraint(constraint);
	}
	
	class MatchResult implements ActionIfFound {
		private Specification spec;
		public Map<Variable, Constant> model;
		public Map<ObjectH, ObjectH> objSrcMap;
		
		public MatchResult(Specification spec) {
			this.spec = spec;
			this.model = new HashMap<>();
			this.objSrcMap = new HashMap<>();
		}
		
		@Override
		public boolean emitMapping(Bijection<ObjectH, ObjectH> isoMap) {
			Map<Variable, Variable> vRenameMap = new HashMap<>();
			for (Entry<ObjectH, ObjectH> e : isoMap.getMapU2V().entrySet()) {
				if (e.getKey().isVariable()) {
					vRenameMap.put(e.getKey().getVariable(), e.getValue().getVariable());
				}
			}
			SMTExpression constraint = new ApplyExpr(SMTOperator.AND,
					WrappedHeap.this.heap.getConstraint().getBody(),
					this.spec.condition.getSubstitution(vRenameMap));
			SMTSolver solver = Options.I().getSMTSolver();
			if (solver.checkSat(constraint, this.model)) {
				for (ObjectH o : this.spec.expcHeap.getAccessibleObjects()) {
					this.objSrcMap.put(o, isoMap.getV(o));
				}
				return true;
			}
			return false;
		}
		
	}
	
	// try to match a test specification
	public MatchResult matchSpecification(Specification spec) {
		if (spec.condition == null) {
			spec.condition = new BoolConst(true);
		}
	 	MatchResult action = new MatchResult(spec);
	 	if (spec.expcHeap.findEmbeddingInto(this.heap, action)) {
	 		return action;
	 	}
		return null;
	}
	
	public boolean isActive() {
		return this.status.equals(HeapStatus.ACTIVE);
	}
	
	public boolean isSubsumed() {
		return this.status.equals(HeapStatus.SUBSUMED);
	}
	
	public List<BackwardRecord> getBackwardRecords() {
		return ImmutableList.copyOf(this.rcdBackwards);
	}
	
	SymbolicHeap getHeap() {
		return this.heap;
	}
	
	ArrayList<Map<Variable, Variable>> getRenameMapList() {
		return this.renameMapList;
	}
	
}
