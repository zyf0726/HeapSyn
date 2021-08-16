package example.kiasan.aatree;

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
import heapsyn.smtlib.SMTSort;
import heapsyn.wrapper.symbolic.SpecFactory;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class AATreeLauncher {

	private static TestGenerator testGenerator;
	
	private static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setHeapScope(AATree.class, 1);
		parms.setHeapScope(AATree.AANode.class, 6);
		List<Method> methods = new ArrayList<>();
		methods.add(AATree.class.getMethod("__new__"));
		methods.add(AATree.class.getMethod("contains", int.class));
		methods.add(AATree.class.getMethod("findMax"));
		methods.add(AATree.class.getMethod("findMin"));
		methods.add(AATree.class.getMethod("insert", int.class));
		methods.add(AATree.class.getMethod("isEmpty"));
		methods.add(AATree.class.getMethod("makeEmpty"));
		methods.add(AATree.class.getMethod("remove", int.class));

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(AATree.class, 1);
		gb.setHeapScope(AATree.AANode.class, 6);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/aatree.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		buildGraph();
		genTestEasy();
		genTestHard();
	}
	
	private static void genTestEasy() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH aatree = specFty.mkRefDecl(AATree.class, "t");
		ObjectH level = specFty.mkVarDecl(SMTSort.INT, "level");
		specFty.addRefSpec("t", "nullNode", "onull", "root", "onull");
		specFty.addRefSpec("onull", "element", "v0", "level", "level",
				"left", "onull", "right", "onull");
		specFty.addVarSpec("(= v0 0)");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, aatree, level);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTestEasy: " + (end - start) + "ms\n");
	}
	
	private static void genTestHard() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH aatree = specFty.mkRefDecl(AATree.class, "t");
		ObjectH l0 = specFty.mkVarDecl(SMTSort.INT, "l0");
		ObjectH l1 = specFty.mkVarDecl(SMTSort.INT, "l1");
		ObjectH l2 = specFty.mkVarDecl(SMTSort.INT, "l2");
		ObjectH l3 = specFty.mkVarDecl(SMTSort.INT, "l3");
		ObjectH l4 = specFty.mkVarDecl(SMTSort.INT, "l4");
		ObjectH l5 = specFty.mkVarDecl(SMTSort.INT, "l5");
		ObjectH v0 = specFty.mkVarDecl(SMTSort.INT, "v0");
		ObjectH v1 = specFty.mkVarDecl(SMTSort.INT, "v1");
		ObjectH v2 = specFty.mkVarDecl(SMTSort.INT, "v2");
		ObjectH v3 = specFty.mkVarDecl(SMTSort.INT, "v3");
		ObjectH v4 = specFty.mkVarDecl(SMTSort.INT, "v4");
		ObjectH v5 = specFty.mkVarDecl(SMTSort.INT, "v5");
		specFty.addRefSpec("t", "root", "o0");
		specFty.addRefSpec("o0", "left", "o1", "right", "o2", "level", "l0", "element", "v0");
		specFty.addRefSpec("o1", "right", "o3", "level", "l1", "element", "v1");
		specFty.addRefSpec("o2", "left", "o4", "right", "o5", "level", "l2", "element", "v2");
		specFty.addRefSpec("o3", "level", "l3", "element", "v3");
		specFty.addRefSpec("o4", "level", "l4", "element", "v4");
		specFty.addRefSpec("o5", "level", "l5", "element", "v5");
		specFty.addVarSpec("(>= (+ v2 v5) 16)");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, aatree,
				v0, v1, v2, v3, v4, v5, l0, l1, l2, l3, l4, l5);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTestHard: " + (end - start) + "ms\n");
	}
	
}
