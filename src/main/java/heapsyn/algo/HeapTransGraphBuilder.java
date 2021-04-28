package heapsyn.algo;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import heapsyn.algo.WrappedHeap.BackwardRecord;
import heapsyn.common.exceptions.UnsupportedPrimitiveType;
import heapsyn.heap.ActionIfFound;
import heapsyn.heap.ClassH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.IntVar;
import heapsyn.util.Bijection;
import heapsyn.util.graph.Edge;
import heapsyn.util.graph.GraphAnalyzer;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.SymbolicExecutor;

public class HeapTransGraphBuilder {
	
	private SymbolicExecutor executor;
	private List<Method> methods;
	private Map<ClassH, Integer> heapScope;
	
	private ArrayList<WrappedHeap> allHeaps;
	private Map<Long, List<WrappedHeap>> activeHeapsByCode;
	
	private GraphAnalyzer<WrappedHeap, Integer> GA;
	
	public HeapTransGraphBuilder(SymbolicExecutor executor, Collection<Method> methods) {
		this.executor = executor;
		this.methods = ImmutableList.copyOf(methods);
		this.heapScope = new HashMap<>();
		this.allHeaps = new ArrayList<>();
		this.activeHeapsByCode = new HashMap<>();
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
	
	private boolean addNewHeap(WrappedHeap newHeap) {
		if (this.isOutOfScope(newHeap.getHeap())) {
			return false;
		}
		this.allHeaps.add(newHeap);
		SymbolicHeap newSymHeap = newHeap.getHeap();
		long code = newSymHeap.getFeatureCode();
		List<WrappedHeap> activeHeaps = this.activeHeapsByCode.get(code);
		if (activeHeaps == null) {
			List<WrappedHeap> heapList = new ArrayList<>();
			heapList.add(newHeap);
			this.activeHeapsByCode.put(code, heapList);
		} else {
			for (WrappedHeap activeHeap : activeHeaps) {
				SymbolicHeap activeSymHeap = activeHeap.getHeap();
				if (!newSymHeap.maybeIsomorphicWith(activeSymHeap))
					continue;
				FindOneMapping action = new FindOneMapping();
				if (newSymHeap.findIsomorphicMappingTo(activeSymHeap, action)) {
					activeHeap.subsumeHeap(newHeap, action.mapping);
					return false;
				}
			}
			activeHeaps.add(newHeap);
		}
		return newHeap.isActive();
	}
	
	public List<WrappedHeap> buildGraph(SymbolicHeap symHeap) {
		Deque<WrappedHeap> heapQueue = new ArrayDeque<>();
		WrappedHeap initHeap = new WrappedHeap(symHeap);
		heapQueue.add(initHeap);
		this.addNewHeap(initHeap);
		while (!heapQueue.isEmpty()) {
			WrappedHeap curHeap = heapQueue.removeFirst();
			for (Method method : this.methods) {
				for (WrappedHeap newHeap : this.tryInvokeMethod(curHeap, method)) 
					if (this.addNewHeap(newHeap))
						heapQueue.addLast(newHeap);
			}
		}
		
		Collection<Edge<WrappedHeap, Integer>> trans = new ArrayList<>();
		for (WrappedHeap heap : this.allHeaps) {
			for (BackwardRecord br : heap.getBackwardRecords())
				trans.add(new Edge<>(br.oriHeap, heap, trans.size()));
		}
		this.GA = new GraphAnalyzer<>(this.allHeaps, trans);
		this.allHeaps.sort((h1, h2) ->
			(this.GA.getSCCIdentifier(h2) - this.GA.getSCCIdentifier(h1))
		);
		int sccBegin = 0;
		while (sccBegin < this.allHeaps.size()) {
			int sccID = this.GA.getSCCIdentifier(this.allHeaps.get(sccBegin));
			int sccEnd = sccBegin;
			while (sccEnd < this.allHeaps.size() &&
					this.GA.getSCCIdentifier(this.allHeaps.get(sccEnd)) == sccID) {
				++sccEnd;
			}
			if (sccEnd - sccBegin > 1) {
				for (int repeat = 0; repeat < 3; ++repeat)
					for (WrappedHeap heap : this.allHeaps.subList(sccBegin, sccEnd))
						heap.recomputeConstraint();
			} else {
				this.allHeaps.get(sccBegin).recomputeConstraint();
			}
			sccBegin = sccEnd;
		}
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
			for (PathDescriptor pd : this.executor.executeMethod(oriHeap.getHeap(), mInvoke))
				finHeaps.add(new WrappedHeap(oriHeap, mInvoke, pd));
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
				int limit = this.heapScope.getOrDefault(cls, 4);
				if (cnt > limit) return true;
				countObjs.put(o.getClassH(), cnt);
			}
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
