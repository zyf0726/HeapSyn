package example.ncll;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import heapsyn.algo.StaticGraphBuilder;
import heapsyn.algo.Statement;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.common.settings.JBSEParameters;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.SMTSort;
import heapsyn.wrapper.symbolic.SpecFactory;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class NcllLauncher {
	
	private static TestGenerator testGenerator;
	
	private static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("hex-settings/ncll-partial.jbse");
		// parms.setSettingsPath("hex-settings/ncll-accurate.jbse");
		parms.setHeapScope(NodeCachingLinkedList.class, 1);
		parms.setHeapScope(NodeCachingLinkedList.LinkedListNode.class, 4);
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
				name -> !name.startsWith("_") || name.equals("_owner"));
		StaticGraphBuilder gb = new StaticGraphBuilder(executor, methods);
		gb.setHeapScope(NodeCachingLinkedList.class, 1);
		gb.setHeapScope(NodeCachingLinkedList.LinkedListNode.class, 8);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap, true);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		StaticGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/ncll.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		buildGraph();
		genTest1();
		genTest2();
		genTest3();
		genTest4();
		genTest(4, 2); 
		genTest(1, 6); 
	}
	
	private static void genTest1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "ncll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		ObjectH cacheSize = specFty.mkVarDecl(SMTSort.INT, "cacheSize");
		specFty.addRefSpec("ncll", "header", "h1",
				"size", "size", "cacheSize", "cacheSize");
		specFty.addRefSpec("h1", "next", "h2");
		specFty.addRefSpec("h2", "next", "h3");
		specFty.addRefSpec("h3", "next", "h4");
		specFty.addRefSpec("h4", "next", "h1");
		specFty.setAccessible("ncll");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll, size, cacheSize);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
	}
	
	private static void genTest2() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "ncll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		ObjectH cacheSize = specFty.mkVarDecl(SMTSort.INT, "cacheSize");
		specFty.addRefSpec("ncll", "firstCachedNode", "c1",
				"size", "size", "cacheSize", "cacheSize");
		specFty.addRefSpec("c1", "next", "c2");
		specFty.addRefSpec("c2", "next", "c3");
		specFty.addRefSpec("c3", "next", "c4");
		specFty.addRefSpec("c4", "next", "c5");
		specFty.addRefSpec("c5", "next", "null");
		specFty.setAccessible("ncll");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll, size, cacheSize);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest2: " + (end - start) + "ms\n");
	}
	
	private static void genTest3() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "ncll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		ObjectH cacheSize = specFty.mkVarDecl(SMTSort.INT, "cacheSize");
		specFty.addRefSpec("ncll", "firstCachedNode", "c1", "header", "o1",
				"size", "size", "cacheSize", "cacheSize");
		specFty.addRefSpec("c1", "next", "c2");
		specFty.addRefSpec("c2", "next", "c3");
		specFty.addRefSpec("c3", "next", "null");
		specFty.addRefSpec("o1", "next", "o2");
		specFty.addRefSpec("o2", "next", "o1");
		specFty.setAccessible("ncll");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll, size, cacheSize);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest3: " + (end - start) + "ms\n");
	}
	
	private static void genTest4() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "ncll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		ObjectH cacheSize = specFty.mkVarDecl(SMTSort.INT, "cacheSize");
		specFty.addRefSpec("ncll", "firstCachedNode", "c1", "header", "o1",
				"size", "size", "cacheSize", "cacheSize");
		specFty.addRefSpec("c1", "next", "c2");
		specFty.addRefSpec("c2", "next", "null");
		specFty.addRefSpec("o1", "next", "o2");
		specFty.addRefSpec("o2", "next", "o3");
		specFty.addRefSpec("o3", "next", "o1");
		specFty.setAccessible("ncll");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll, size, cacheSize);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest4: " + (end - start) + "ms\n");
	}
	
	private static void genTest(int expcSize, int expcCacheSize) {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH ncll = specFty.mkRefDecl(NodeCachingLinkedList.class, "ncll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		ObjectH cacheSize = specFty.mkVarDecl(SMTSort.INT, "cacheSize");
		specFty.addRefSpec("ncll", "size", "size", "cacheSize", "cacheSize");
		specFty.setAccessible("ncll");
		specFty.addVarSpec("(= size " + expcSize + ")");
		specFty.addVarSpec("(= cacheSize " + expcCacheSize + ")");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, ncll, size, cacheSize);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.print(">> genTest(" + expcSize + ", " + expcCacheSize + "): ");
		System.out.println((end - start) + "ms\n");
	}
	
}
