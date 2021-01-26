package heapsyn.algo;
/*
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import heapsyn.heap.ObjectH;
import heapsyn.smtlib.IntVar;
import heapsyn.util.Bijection;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.SymbolicExecutor;

public class HeapTransGraphBuilder {
	
	private SymbolicExecutor symExecutor;
	private ArrayList<Method> canMethods;
	
	private ArrayList<SymbolicHeapInGraph> allHeaps;
	
	public HeapTransGraphBuilder(SymbolicExecutor symExecutor, Collection<Method> canMethods) {
		this.symExecutor = symExecutor;
		this.canMethods = new ArrayList<>(canMethods);
		this.allHeaps = new ArrayList<>();
	}
	
	public void buildGraph() {
		this.allHeaps.add(SymbolicHeapInGraph.EMPTY_HEAP);
		Deque<SymbolicHeapInGraph> heapQueue = new ArrayDeque<>(this.allHeaps);
		while (!heapQueue.isEmpty()) {
			SymbolicHeapInGraph curHeap = heapQueue.removeFirst();
			curHeap.recomputeConstraint();
//			curHeap.__debugPrintOut(System.err);
			if (curHeap.minSeqLen >= 5)
				continue;
			
			for (Method method : this.canMethods) {
				for (SymbolicHeapInGraph newHeap : tryInvokeMethod(curHeap, method)) {
					this.allHeaps.add(newHeap);
					int countIsomorphic = 0;
					for (SymbolicHeapInGraph activeHeap : heapQueue) {
						if (!newHeap.maybeIsomorphic(activeHeap))
							continue;
						Bijection<ObjectH, ObjectH> isoMap = newHeap.getIsomorphicMapping(activeHeap);
						if (isoMap != null) {
							countIsomorphic += 1;
							activeHeap.involveIsomorphicHeap(newHeap, isoMap);
						}
					}
					if (countIsomorphic > 0) {
						newHeap.makeDeprecated();
						assert(countIsomorphic == 1);
					} else {
						heapQueue.addLast(newHeap);
					}
				}
			}
		}
		System.out.println("number = " + this.allHeaps.size());
		int countNonIso = 0;
		for (int i = 0; i < this.allHeaps.size(); ++i) {
			SymbolicHeapInGraph curHeap = this.allHeaps.get(i);
			boolean isIso = false;
			for (int j = 0; j < i; ++j) {
				SymbolicHeapInGraph existedHeap = this.allHeaps.get(j);
				if (curHeap.maybeIsomorphic(existedHeap) && curHeap.getIsomorphicMapping(existedHeap) != null) {
					isIso = true;
					break;
				}
			}
			if (!isIso) {
				countNonIso += 1;
				System.out.print(">>");
			}
			curHeap.__debugPrintOut(System.out);
		}
		System.out.println("countNonIso = " + countNonIso);
	}
	
	private List<SymbolicHeapInGraph>
	tryInvokeMethod(SymbolicHeapInGraph initHeap, Method method) {
		ArrayList<Class<?>> paraTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
		if (!Modifier.isStatic(method.getModifiers())) {
			paraTypes.add(0, method.getDeclaringClass());
		}
		List<ArrayList<ObjectH>> invokeArgSeqs = fillInvokeArguments(
				paraTypes.size(), paraTypes, initHeap.getAllObjects()
		);
		List<SymbolicHeapInGraph> heaps = new ArrayList<>();
		for (ArrayList<ObjectH> invokeArgSeq : invokeArgSeqs) {
			MethodInvoke mInvoke = new MethodInvoke(method, invokeArgSeq);
			for (PathDescriptor pd : this.symExecutor.executeMethod(initHeap, mInvoke)) {
				ArrayList<ObjectH> accObjs = new ArrayList<>();
				for (ObjectH o : pd.allObjs) {
					if (initHeap.isAccessible(pd.objSrcMap.get(o)))
						accObjs.add(o);
				}
				if (pd.returnVal != null) {
					accObjs.add(pd.returnVal);
				}
				SymbolicHeapInGraph finalHeap = new SymbolicHeapInGraph(
						accObjs, initHeap, mInvoke,
						pd.pathCond, pd.returnVal, pd.objSrcMap, pd.varExprMap
				);
				heaps.add(finalHeap);
			}
		}
		return heaps;
	}
	
	private List<ArrayList<ObjectH>>
	fillInvokeArguments(int depth, ArrayList<Class<?>> paraTypes, Collection<ObjectH> objs) {
		if (depth == 0) {
			List<ArrayList<ObjectH>> invokeArgSeqs = new ArrayList<>();
			invokeArgSeqs.add(new ArrayList<>());
			return invokeArgSeqs;
		}
		Class<?> paraType = paraTypes.get(depth - 1);
		if (paraType.isPrimitive()) {
			List<ArrayList<ObjectH>> invokeArgSeqs =
					fillInvokeArguments(depth - 1, paraTypes, objs);
			ObjectH arg = new ObjectH(new IntVar()); // TODO if not IntVariable?
			for (ArrayList<ObjectH> invokeArgSeq : invokeArgSeqs)
				invokeArgSeq.add(arg);
			return invokeArgSeqs;
		} else {
			List<ArrayList<ObjectH>> result = new ArrayList<>();
			for (ObjectH arg : objs) {
				if (!arg.isNullObject() && arg.getClassH().getJavaClass() != paraType)
					continue;
				Collection<ArrayList<ObjectH>> invokeArgSeqs =
						fillInvokeArguments(depth - 1, paraTypes, objs);
				for (ArrayList<ObjectH> invokeArgSeq : invokeArgSeqs)
					invokeArgSeq.add(arg);
				result.addAll(invokeArgSeqs);
			}
			return result;
		}
	}

}
*/