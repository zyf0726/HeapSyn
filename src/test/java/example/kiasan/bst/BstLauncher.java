package example.kiasan.bst;

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

public class BstLauncher {

	private static TestGenerator testGenerator;
	
	private static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("hex-settings/kiasan.jbse");
		parms.setHeapScope(BinarySearchTree.class, 1);
		parms.setHeapScope(BinaryNode.class, 5);
		List<Method> methods = new ArrayList<>();
		methods.add(BinarySearchTree.class.getMethod("__new__"));
		methods.add(BinarySearchTree.class.getMethod("find", int.class));
		methods.add(BinarySearchTree.class.getMethod("findMax"));
		methods.add(BinarySearchTree.class.getMethod("findMin"));
		methods.add(BinarySearchTree.class.getMethod("insert", int.class));
		methods.add(BinarySearchTree.class.getMethod("isEmpty"));
		methods.add(BinarySearchTree.class.getMethod("makeEmpty"));
		methods.add(BinarySearchTree.class.getMethod("remove", int.class));

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		StaticGraphBuilder gb = new StaticGraphBuilder(executor, methods);
		gb.setHeapScope(BinarySearchTree.class, 1);
		gb.setHeapScope(BinaryNode.class, 5);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap, true);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		StaticGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/bst.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		buildGraph();
		genTest0();
		genTest3();
		genTest5();
	}
	
	private static void genTest0() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH bst = specFty.mkRefDecl(BinarySearchTree.class, "t");
		specFty.addRefSpec("t", "root", "null");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, bst);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest0: " + (end - start) + "ms\n");
	}
	
	private static void genTest3() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH bst = specFty.mkRefDecl(BinarySearchTree.class, "t");
		ObjectH v2 = specFty.mkVarDecl(SMTSort.INT, "v2");
		ObjectH v3 = specFty.mkVarDecl(SMTSort.INT, "v3");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "left", "o2", "right", "o3");
		specFty.addRefSpec("o2", "element", "v2");
		specFty.addRefSpec("o3", "element", "v3");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, bst, v2, v3);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest3: " + (end - start) + "ms\n");
	}
	
	private static void genTest5() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH bst = specFty.mkRefDecl(BinarySearchTree.class, "t");
		specFty.addRefSpec("t", "root", "o1");
		specFty.addRefSpec("o1", "left", "o2", "right", "o3");
		specFty.addRefSpec("o2", "left", "o4", "right", "null", "element", "v2");
		specFty.addRefSpec("o3", "element", "v3");
		specFty.addRefSpec("o4", "left", "o5");
		specFty.addRefSpec("o5", "left", "null", "right", "null");
		specFty.addVarSpec("(= 2021 (+ v2 v3))");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, bst);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest5: " + (end - start) + "ms\n");
	}
}
