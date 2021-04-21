package example.avl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import heapsyn.algo.HeapTransGraphBuilder;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.common.settings.JBSEParameters;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;
import heapsyn.wrapper.symbolic.SymbolicHeapWithJBSE;

public class AvlLauncher {
	
	private static TestGenerator testGenerator;
	
	private static void buildGraph(Collection<Method> methods) {
		long start = System.currentTimeMillis();
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(
				new SymbolicExecutorWithCachedJBSE(), methods);
		SymbolicHeap initHeap = new SymbolicHeapWithJBSE(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		testGenerator = new TestGenerator(heaps);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.print("number of symbolic execution = ");
		System.out.println(SymbolicExecutorWithCachedJBSE.__countExecution);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) {
		JBSEParameters parms = JBSEParameters.I();
		parms.setSettingsPath("HEXsettings/avltree.jbse");
		parms.setHeapScope(AvlNode.class, 5);
		List<Method> methods = new ArrayList<>();
		try {
			methods.add(AvlTree.class.getMethod("__new__"));
			// methods.add(AvlTree.class.getMethod("find", int.class));
			methods.add(AvlTree.class.getMethod("findMax"));
			// methods.add(AvlTree.class.getMethod("findMin"));
			// methods.add(AvlTree.class.getMethod("insertElem", int.class));
			// methods.add(AvlTree.class.getMethod("isEmpty"));
			// methods.add(AvlTree.class.getMethod("makeEmpty"));
		} catch (NoSuchMethodException e) {
			System.err.println("NoSuchMethodException: " + e.getMessage());
			System.exit(-1);
		}
		buildGraph(methods);
	}

}
