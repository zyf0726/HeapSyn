package example.treemap;

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

public class TreeMapLauncher {
	
	private static TestGenerator testGenerator;
	
	public static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("HexSettings/treemap.jbse");
		parms.setHeapScope(TreeMap.class, 1);
		parms.setHeapScope(TreeMap.Entry.class, 5);
		parms.setDepthScope(500);
		parms.setCountScope(6000);
		List<Method> methods = new ArrayList<>();
		methods.add(TreeMap.class.getMethod("__new__"));
		methods.add(TreeMap.class.getMethod("put", int.class, Object.class));
		methods.add(TreeMap.class.getMethod("get", int.class));
		methods.add(TreeMap.class.getMethod("remove", int.class));
		methods.add(TreeMap.class.getMethod("clear"));
		methods.add(TreeMap.class.getMethod("size"));
		methods.add(TreeMap.class.getMethod("containsKey", int.class));
		methods.add(TreeMap.class.getMethod("containsValue", Object.class));
		methods.add(TreeMap.class.getMethod("firstKey"));
		methods.add(TreeMap.class.getMethod("lastKey"));

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE(
				name -> !name.startsWith("_"));
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(TreeMap.class, 1);
		gb.setHeapScope(TreeMap.Entry.class, 6);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/treemap.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		buildGraph();
		genTest1();
		genTest4$1();
		genTest4$2();
		genTest5$1();
		genTest6$1();
		genTest6$2();
	}
	
	public static void genTest1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "t");
		specFty.addRefSpec("t", "root", "null", "size", "size");
		specFty.addVarSpec("(>= size 0)");
		
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, treemap);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
	}
	
	public static void genTest4$1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "t");
		ObjectH v1 = specFty.mkRefDecl(Object.class, "v1");
		ObjectH v2 = specFty.mkRefDecl(Object.class, "v2");
		ObjectH v3 = specFty.mkRefDecl(Object.class, "v3");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "parent", "null", "left", "o2", "right", "o3", "value", "v1");
		specFty.addRefSpec("o2", "parent", "o1", "left", "null", "right", "o4", "value", "null");
		specFty.addRefSpec("o4", "parent", "o2", "left", "null", "right", "null", "value", "v1");
		specFty.addRefSpec("o3", "parent", "o1", "value", "v2");
		specFty.setAccessible("t", "v1", "v2", "v3");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, treemap, v1, v2, v3);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest4$1: " + (end - start) + "ms\n");
	}
	
	public static void genTest4$2() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "t");
		ObjectH v1 = specFty.mkRefDecl(Object.class, "v1");
		ObjectH v2 = specFty.mkRefDecl(Object.class, "v2");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "parent", "null", "left", "o2", "right", "o3", "value", "null");
		specFty.addRefSpec("o2", "parent", "o1", "left", "null", "right", "null", "value", "null");
		specFty.addRefSpec("o3", "parent", "o1", "left", "null", "right", "o4", "value", "v1");
		specFty.addRefSpec("o4", "parent", "o3", "value", "v2");
		specFty.setAccessible("t", "v2");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, treemap, v1, v2);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest4$2: " + (end - start) + "ms\n");
	}
	
	public static void genTest5$1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "t");
		ObjectH v1 = specFty.mkRefDecl(Object.class, "v1");
		ObjectH v3 = specFty.mkRefDecl(Object.class, "v3");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "parent", "null", "left", "o2", "right", "o3", "value", "v1");
		specFty.addRefSpec("o2", "left", "o4", "right", "o5", "value", "null");
		specFty.addRefSpec("o4", "parent", "o2", "left", "null", "right", "null", "value", "v2");
		specFty.addRefSpec("o5", "parent", "o2", "value", "v1");
		specFty.addRefSpec("o3", "left", "null", "right", "null", "value", "v2");
		specFty.setAccessible("t", "v1", "v3");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, treemap, v1, v3);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest5$1: " + (end - start) + "ms\n");
	}

	public static void genTest6$1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "t");
		ObjectH v0 = specFty.mkRefDecl(Object.class, "v0");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "left", "o2", "right", "o3");
		specFty.addRefSpec("o2", "left", "o4", "right", "o5", "color", "b2");
		specFty.addVarSpec("(= b2 false)");  // RED
		specFty.addRefSpec("o3", "left", "null", "right", "null", "color", "b3");
		specFty.addVarSpec("(= b3 true)");   // BLACK
		specFty.addRefSpec("o4", "left", "null", "right", "null");
		specFty.addRefSpec("o5", "left", "null", "right", "null");
		specFty.setAccessible("t", "v0");
		Specification spec = specFty.genSpec();
		// +50 +30 +70 +80 -80 +10 +40 +20 -20
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, treemap, v0);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest6$1: " + (end - start) + "ms\n");
	}
	
	public static void genTest6$2() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "t");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "left", "o2", "right", "o4", "color", "b1");
		specFty.addVarSpec("(= b1 true)");   // BLACK
		specFty.addRefSpec("o2", "left", "o3", "color", "b2");
		specFty.addVarSpec("(= b2 true)");   // BLACK
		specFty.addRefSpec("o3", "color", "b3");
		specFty.addVarSpec("(= b3 false)");  // RED
		specFty.addRefSpec("o4", "color", "b4");
		specFty.addVarSpec("(= b4 false)");  // RED
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		// +50 +30 +70 +80 +60 +90 -90 +20
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, treemap);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest6$2: " + (end - start) + "ms\n");
	}
}
