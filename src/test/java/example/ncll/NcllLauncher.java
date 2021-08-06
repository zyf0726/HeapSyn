package example.ncll;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import heapsyn.algo.HeapTransGraphBuilder;
import heapsyn.algo.Statement;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.common.settings.JBSEParameters;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SpecFactory;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class NcllLauncher {
	private static TestGenerator testGenerator;
	public List<Statement> stmts;
	
	public static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("HexSettings/ncll.jbse");
		parms.setHeapScope(NodeCachingLinkedList.class, 1);
		parms.setHeapScope(NodeCachingLinkedList.LinkedListNode.class, 8);
		parms.setDepthScope(50);
		parms.setCountScope(600);
		List<Method> methods = new ArrayList<>();
		methods.add(NodeCachingLinkedList.class.getMethod("__new__"));
		methods.add(NodeCachingLinkedList.class.getMethod("add", Object.class));
		methods.add(NodeCachingLinkedList.class.getMethod("remove", Object.class));
		methods.add(NodeCachingLinkedList.class.getMethod("addLast", Object.class));
		methods.add(NodeCachingLinkedList.class.getMethod("removeIndex", int.class));
		methods.add(NodeCachingLinkedList.class.getMethod("contains", Object.class));
		methods.add(NodeCachingLinkedList.class.getMethod("indexOf", Object.class));

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE(
				name -> !name.startsWith("_"));
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(NodeCachingLinkedList.class, 1);
		gb.setHeapScope(NodeCachingLinkedList.LinkedListNode.class, 8);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/ncll.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
		
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		buildGraph();
		genTest1();
		genTest2();
	}
	
	public static void genTest1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "o0");
		specFty.addRefSpec("o0", "header", "o1");
		specFty.addRefSpec("o1", "next", "o2");
		specFty.addRefSpec("o2", "next", "o3");
		specFty.addRefSpec("o3", "next", "o1", "previous","o2");
		specFty.setAccessible("o0");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
	}
	
	public static void genTest2() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "o0");
		specFty.addRefSpec("o0", "maximumCacheSize", "s0", "cacheSize", "s1",
				"firstCachedNode", "o1");
		specFty.addRefSpec("o1", "next", "o2");
		specFty.addRefSpec("o2", "next", "o3");
		specFty.addRefSpec("o3", "next", "o4");
		specFty.addRefSpec("o4", "next", "o5");
		specFty.addRefSpec("o5", "next", "o6");
		specFty.addRefSpec("o6", "next", "null");
		specFty.setAccessible("o0");
		// specFty.addVarSpec("(= s1 s0)");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest2: " + (end - start) + "ms\n");
	}
	
}
