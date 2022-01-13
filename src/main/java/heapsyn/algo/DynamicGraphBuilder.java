package heapsyn.algo;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import heapsyn.algo.WrappedHeap.BackwardRecord;
import heapsyn.algo.WrappedHeap.ForwardRecord;
import heapsyn.common.exceptions.UnsupportedPrimitiveType;
import heapsyn.common.settings.Options;
import heapsyn.heap.ActionIfFound;
import heapsyn.heap.ClassH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;
import heapsyn.util.UniqueQueue;
import heapsyn.wrapper.smt.SMTSolver;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.SymbolicExecutor;

import static heapsyn.algo.WrappedHeap.deriveVariableMapping;

public class DynamicGraphBuilder {
	
	private SMTSolver solver;
	private SymbolicExecutor executor;
	private List<Method> methods;
	private Map<ClassH, Integer> heapScope;
	
	private ArrayList<WrappedHeap> allHeaps;
	private Map<Long, List<WrappedHeap>> activeHeapsByCode;
	
	private UniqueQueue<WrappedHeap> heapsToExpand;
	
	public DynamicGraphBuilder(SymbolicExecutor executor, Collection<Method> methods) {
		this.solver = Options.I().getSMTSolver();
		this.executor = executor;
		this.methods = ImmutableList.copyOf(methods);
		this.heapScope = new HashMap<>();
		this.allHeaps = new ArrayList<>();
		this.activeHeapsByCode = new HashMap<>();
		this.heapsToExpand = new UniqueQueue<>();
	}
	
	public void setHeapScope(Class<?> javaClass, int scope) {
		this.heapScope.put(ClassH.of(javaClass), scope);
	}
	
	class FindOneMapping implements ActionIfFound {
		Bijection<ObjectH, ObjectH> mapping = null;
		@Override
		public boolean emitMapping(Bijection<ObjectH, ObjectH> ret) {
			this.mapping = ret;
			return true;
		}
	}
	
	private void addNewHeap(WrappedHeap newHeap) {
		assert(!this.isOutOfScope(newHeap.getHeap()));
		assert(newHeap.isActive());
		SymbolicHeap newSymHeap = newHeap.getHeap();
		long code = newSymHeap.getFeatureCode();
		List<WrappedHeap> activeHeaps = this.activeHeapsByCode.get(code);
		if (activeHeaps == null) {
			this.activeHeapsByCode.put(code, Lists.newArrayList(newHeap));
		} else {
			boolean subsumed = false;
			for (WrappedHeap activeHeap : activeHeaps) {
				SymbolicHeap activeSymHeap = activeHeap.getHeap();
				if (!newSymHeap.maybeIsomorphicWith(activeSymHeap))
					continue;
				FindOneMapping action = new FindOneMapping();
				if (!newSymHeap.findIsomorphicMappingTo(activeSymHeap, action))
					continue;
				if (newHeap.likelyEntails(activeHeap, action.mapping, this.solver)) {
					newHeap.setRedundant();
					return;
				} else {
					subsumed = true;
					activeHeap.subsumeHeap(newHeap, action.mapping);
				}
			}
			if (!subsumed) activeHeaps.add(newHeap);
		}
		this.allHeaps.add(newHeap);
	}
	
	private void expandHeaps(int maxLength) {
		System.err.println(">> maxLength = " + maxLength);
		while (!this.heapsToExpand.isEmpty()) {
			WrappedHeap curHeap = this.heapsToExpand.element();
			assert(curHeap.isActive());
			int curLength = curHeap.curLength;
			if (curLength >= maxLength) break;
			System.err.println(curHeap.__debugGetName() + " " + curLength);
			this.heapsToExpand.remove();
			curHeap.recomputeConstraint();
			List<WrappedHeap> finHeaps = new ArrayList<>();
			if (curHeap.isEverExpanded) {
				curHeap.getForwardRecords().forEach(fr -> finHeaps.add(fr.finHeap));
			} else {
				for (Method method : this.methods) {
					for (WrappedHeap newHeap : this.tryInvokeMethod(curHeap, method)) {
						newHeap.setUnsat();
						finHeaps.add(newHeap);
					}
				}
			}
			for (WrappedHeap finHeap : finHeaps) {
				BackwardRecord br = finHeap.getBackwardRecords().stream()
						.filter(r -> r.oriHeap == curHeap).findAny().get();
				List<SMTExpression> clauses = new ArrayList<>();
				clauses.add(curHeap.getHeap().getConstraint().getBody());
				if (br.pathCond != null) {
					clauses.add(br.pathCond);
				}
				for (Variable var : finHeap.getHeap().getVariables()) {
					if (br.varExprMap.containsKey(var)) {
						SMTExpression clause = new ApplyExpr(SMTOperator.BIN_EQ,
								var, br.varExprMap.get(var));
						clauses.add(clause);
					}
				}
				SMTExpression cond = new ApplyExpr(SMTOperator.AND, clauses);
				Map<Variable, Constant> solverModel = new HashMap<>();
				if (!this.solver.checkSat(cond, solverModel)) {
					assert(finHeap.isUnsat());
					continue;
				}
				finHeap.addSampleModel(solverModel);
				if (finHeap.isUnsat() || finHeap.isRedundant()) {
					finHeap.setActive();
					this.addNewHeap(finHeap);
				}
				if (finHeap.isRedundant()) continue;
				WrappedHeap succHeap = null;
				if (finHeap.isSubsumed()) {
					finHeap.recomputeConstraint();
					ForwardRecord fr = finHeap.getForwardRecords().get(0);
					succHeap = fr.finHeap;
					Map<Variable, Variable> varMapping = deriveVariableMapping(fr.mapping);
					for (Entry<Variable, Variable> entry : varMapping.entrySet()) {
						if (solverModel.containsKey(entry.getKey())) {
							solverModel.put(entry.getValue(), solverModel.get(entry.getKey()));
						}
					}
					succHeap.addSampleModel(solverModel);
				} else {
					assert(finHeap.isActive());
					succHeap = finHeap;
				}
				if (!heapsToExpand.contains(succHeap)) {
					this.heapsToExpand.add(succHeap);
					succHeap.curLength = curLength + 1;
				}
			}
			curHeap.isEverExpanded = true;
		}
		System.err.println("-----------------");
		for (WrappedHeap heap : this.heapsToExpand) {
			System.err.println(heap.__debugGetName() + " " + heap.curLength);
			if (heap.curLength == maxLength)
				heap.recomputeConstraint();
		}
	}

	public List<WrappedHeap> buildGraph(SymbolicHeap symHeap, int maxSeqLen) {
		WrappedHeap initHeap = new WrappedHeap(symHeap);
		this.heapsToExpand.add(initHeap);
		this.addNewHeap(initHeap);
		initHeap.curLength = 0;
		this.expandHeaps(maxSeqLen);
		return ImmutableList.copyOf(this.allHeaps);
	}
	
	private Collection<WrappedHeap>
	tryInvokeMethod(WrappedHeap oriHeap, Method method) {
		ArrayList<Class<?>> paraTypes = Lists.newArrayList(method.getParameterTypes());
		if (!Modifier.isStatic(method.getModifiers())) {
			paraTypes.add(0, method.getDeclaringClass());
		}
		Collection<ArrayList<ObjectH>> invokeArgSeqs = fillInvokeArguments(
				paraTypes.size(), paraTypes, oriHeap.getHeap().getAccessibleObjects());
		Collection<WrappedHeap> finHeaps = new ArrayList<>();
		for (ArrayList<ObjectH> invokeArgSeq : invokeArgSeqs) {
			MethodInvoke mInvoke = new MethodInvoke(method, invokeArgSeq);
			for (PathDescriptor pd : this.executor.executeMethod(oriHeap.getHeap(), mInvoke)) {
				if (!this.isOutOfScope(pd.finHeap))
					finHeaps.add(new WrappedHeap(oriHeap, mInvoke, pd));
			}
		}
		return finHeaps;
	}
	
	private static Collection<ArrayList<ObjectH>>
	fillInvokeArguments(int nRemain, ArrayList<Class<?>> paraTypes, Collection<ObjectH> objs) {
		if (nRemain == 0) {
			return Collections.singletonList(Lists.newArrayList());
		}
		Collection<ArrayList<ObjectH>> argSeqs =
				fillInvokeArguments(nRemain - 1, paraTypes, objs);
		Class<?> paraType = paraTypes.get(nRemain - 1);
		if (paraType.isPrimitive()) {
			String typeName = paraType.getName();
			ObjectH arg = null;
			if (Arrays.asList("byte", "char", "int", "long", "short").contains(typeName)) {
				arg = new ObjectH(new IntVar());
			} else if ("boolean".equals(typeName)) {
				arg = new ObjectH(new BoolVar());
			} else {
				throw new UnsupportedPrimitiveType(typeName);
			}
			for (ArrayList<ObjectH> argSeq : argSeqs)
				argSeq.add(arg);
			return argSeqs;
		} else if (paraType == Object.class) {
			ObjectH arg = new ObjectH(ClassH.of(paraType), Collections.emptyMap());
			for (ArrayList<ObjectH> argSeq : argSeqs)
				argSeq.add(arg);
			return argSeqs;
		} else {
			Collection<ArrayList<ObjectH>> extArgSeqs = new ArrayList<>();
			for (ArrayList<ObjectH> argSeq : argSeqs) {
				for (ObjectH arg : objs) {
					if (arg.isNullObject() || paraType.equals(arg.getClassH().getJavaClass())) {
						ArrayList<ObjectH> extArgSeq = new ArrayList<>(argSeq);
						extArgSeq.add(arg);
						extArgSeqs.add(extArgSeq);
					}
				}
			}
			return extArgSeqs;
		}
	}
	
	
	private boolean isOutOfScope(SymbolicHeap heap) {
		Map<ClassH, Integer> countObjs = new HashMap<>();
		for (ObjectH o : heap.getAllObjects()) {
			if (o.isNonNullObject()) {
				ClassH cls = o.getClassH();
				int cnt = countObjs.getOrDefault(cls, 0) + 1;
				int limit = this.heapScope.getOrDefault(cls, 4); // TODO
				if (cnt > limit) return true;
				countObjs.put(o.getClassH(), cnt);
			}
		}
		ClassH clsObject = ClassH.of(Object.class);
		if (this.heapScope.containsKey(clsObject)) {
			long cnt = heap.getAllObjects().stream()
					.filter(o -> o.getClassH() == clsObject).count();
			return cnt > this.heapScope.get(clsObject);
		}
		return false;
	}
	
	public static void __debugPrintOut(List<WrappedHeap> heaps,
			SymbolicExecutor executor, PrintStream ps) {
		long countActive = heaps.stream().filter(o -> o.isActive()).count();
		ps.println("number of active/all heaps = " + countActive + "/" + heaps.size());
		long countTrans = heaps.stream()
				.map(o -> o.getBackwardRecords().size())
				.reduce(0, (a, b) -> a + b);
		ps.println("number of transitions = " + countTrans);
		ps.println("number of symbolic executions = " + executor.getExecutionCount());
		ps.println();
		for (WrappedHeap heap : heaps)
			heap.__debugPrintOut(ps);
	}
	
}
