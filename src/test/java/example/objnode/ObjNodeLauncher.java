package example.objnode;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import heapsyn.algo.HeapTransGraphBuilder;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class ObjNodeLauncher {
	
	private static TestGenerator buildGraph(Collection<Method> methods, String outfile)
			throws FileNotFoundException {
		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(ObjNode.class, 3);
		gb.setHeapScope(Object.class, 4);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream(outfile));
		TestGenerator testgen = new TestGenerator(heaps);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
		return testgen;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		List<Method> allMethods = Lists.newArrayList(
				ObjNode.mNewAlias, ObjNode.mNewNull, ObjNode.mNewFresh,
				ObjNode.mGetNext, ObjNode.mGetValue, 
				ObjNode.mSetValue, ObjNode.mSetFreshValue, ObjNode.mResetValue,
				ObjNode.mAddBefore, ObjNode.mAddAfter,
				ObjNode.mSetValueAliasNext, ObjNode.mMakeValueFresh
		);
		buildGraph(allMethods, "tmp/objnode.txt");
	}

}
