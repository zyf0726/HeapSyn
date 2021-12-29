package heapsyn.algo;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import heapsyn.algo.WrappedHeap.ForwardRecord;
import heapsyn.common.exceptions.UnsupportedPrimitiveType;
import heapsyn.common.settings.Options;
import heapsyn.heap.ActionIfFound;
import heapsyn.heap.ClassH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.util.Bijection;
import heapsyn.util.UniqueQueue;
import heapsyn.wrapper.smt.SMTSolver;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.SymbolicExecutor;

public class HeapTransGraphBuilderIncr {
	
	private SMTSolver solver;
	private SymbolicExecutor executor;
	private List<Method> methods;
	private Map<ClassH, Integer> heapScope;
	
	private ArrayList<WrappedHeap> allHeaps;
	private Map<Long, List<WrappedHeap>> reprHeapsByCode;
	
	private UniqueQueue<WrappedHeap> heapsToExpand;
	private Set<WrappedHeap> heapsEverExpanded;
	private Map<WrappedHeap, Integer> seqLenByHeap;
	
	public HeapTransGraphBuilderIncr(SymbolicExecutor executor, Collection<Method> methods) {
		this.solver = Options.I().getSMTSolver();
		this.executor = executor;
		this.methods = ImmutableList.copyOf(methods);
		this.heapScope = new HashMap<>();
		this.allHeaps = new ArrayList<>();
		this.reprHeapsByCode = new HashMap<>();
		this.heapsToExpand = new UniqueQueue<>();
		this.heapsEverExpanded = new HashSet<>();
		this.seqLenByHeap = new HashMap<>();
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
	
	private WrappedHeap addNewHeap(WrappedHeap newHeap) {
		assert(!this.isOutOfScope(newHeap.getHeap()));
		this.allHeaps.add(newHeap);
		SymbolicHeap newSymHeap = newHeap.getHeap();
		long code = newSymHeap.getFeatureCode();
		List<WrappedHeap> reprHeaps = this.reprHeapsByCode.get(code);
		if (reprHeaps == null) {
			this.reprHeapsByCode.put(code, Lists.newArrayList(newHeap));
		} else {
			for (WrappedHeap reprHeap : reprHeaps) {
				SymbolicHeap reprSymHeap = reprHeap.getHeap();
				if (!newSymHeap.maybeIsomorphicWith(reprSymHeap))
					continue;
				FindOneMapping action = new FindOneMapping();
				if (!newSymHeap.findIsomorphicMappingTo(reprSymHeap, action))
					continue;
				reprHeap.subsumeHeap(newHeap, action.mapping);
				return reprHeap;
			}
			reprHeaps.add(newHeap);
		}
		return newHeap;
	}
	
	private void expandHeaps(int maxSeqLen) {
		System.err.println(">> maxSeqLen = " + maxSeqLen);
		while (!this.heapsToExpand.isEmpty()) {
			WrappedHeap curHeap = this.heapsToExpand.element();
			int curLen = this.seqLenByHeap.get(curHeap);
			if (curLen >= maxSeqLen) break;
			System.err.println(curHeap.__debugGetName() + " " + curLen);
			this.heapsToExpand.remove();
			this.seqLenByHeap.remove(curHeap);
			curHeap.recomputeConstraint();
			if (this.heapsEverExpanded.contains(curHeap)) {
				for (ForwardRecord fr : curHeap.getForwardRecords()) {
					if (fr.pathCond != null) {
						SMTExpression cond = new ApplyExpr(SMTOperator.AND,
								curHeap.getHeap().getConstraint().getBody(), fr.pathCond);
						if (!this.solver.checkSat(cond, null)) {
							assert(fr.finHeap.isUnsat());
							continue;
						}
					}
					if (fr.finHeap.isSubsumed()) {
						fr.finHeap.recomputeConstraint();
						assert(fr.finHeap.getForwardRecords().size() == 1);
						WrappedHeap isoHeap = fr.finHeap.getForwardRecords().get(0).finHeap;
						this.heapsToExpand.add(isoHeap);
						this.seqLenByHeap.putIfAbsent(isoHeap, curLen + 1);
					} else if (fr.finHeap.isUnsat()) {
						fr.finHeap.setActive();
						WrappedHeap reprHeap = this.addNewHeap(fr.finHeap);
						if (reprHeap != fr.finHeap) {
							fr.finHeap.recomputeConstraint();
						}
						this.heapsToExpand.add(reprHeap);
						this.seqLenByHeap.putIfAbsent(reprHeap, curLen + 1);
					} else {
						this.heapsToExpand.add(fr.finHeap);
						this.seqLenByHeap.putIfAbsent(fr.finHeap, curLen + 1);
					}
				}
			} else {
				for (Method method : this.methods) {
					for (WrappedHeap newHeap : this.tryInvokeMethod(curHeap, method)) {
						assert(newHeap.getBackwardRecords().size() == 1);
						boolean isSat = true;
						if (newHeap.getBackwardRecords().get(0).pathCond != null) {
							SMTExpression cond = new ApplyExpr(SMTOperator.AND,
									curHeap.getHeap().getConstraint().getBody(),
									newHeap.getBackwardRecords().get(0).pathCond);
							isSat = this.solver.checkSat(cond, null);
						}
						if (isSat) {
							WrappedHeap reprHeap = this.addNewHeap(newHeap);
							if (reprHeap != newHeap) {
								newHeap.recomputeConstraint();
							}
							this.heapsToExpand.add(reprHeap);
							this.seqLenByHeap.putIfAbsent(reprHeap, curLen + 1);
						} else {
							newHeap.setUnsat();
						}
					}
				}
				heapsEverExpanded.add(curHeap);
			}
		}
		for (WrappedHeap heap : this.heapsToExpand) {
			System.err.println(heap.__debugGetName() + " " + this.seqLenByHeap.get(heap));
			if (this.seqLenByHeap.get(heap) == maxSeqLen)
				heap.recomputeConstraint();
		}
	}
	
	public List<WrappedHeap> buildGraph(SymbolicHeap symHeap, int maxSeqLen) {
		WrappedHeap initHeap = new WrappedHeap(symHeap);
		this.heapsToExpand.add(initHeap);
		this.seqLenByHeap.put(initHeap, 0);
		this.addNewHeap(initHeap);
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
