package example.kiasan.leftist;

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

public class LeftistLauncher {

	private static TestGenerator testGenerator;
	
	private static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setHeapScope(LeftistHeap.class, 2);
		parms.setHeapScope(LeftistHeap.LeftistNode.class, 6);
		List<Method> methods = new ArrayList<>();
		methods.add(LeftistHeap.class.getMethod("__new__"));
		methods.add(LeftistHeap.class.getMethod("deleteMin"));
		methods.add(LeftistHeap.class.getMethod("findMin"));
		methods.add(LeftistHeap.class.getMethod("insert", int.class));
		methods.add(LeftistHeap.class.getMethod("isEmpty"));
		methods.add(LeftistHeap.class.getMethod("makeEmpty"));
		methods.add(LeftistHeap.class.getMethod("merge", LeftistHeap.class));

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(LeftistHeap.class, 2);
		gb.setHeapScope(LeftistHeap.LeftistNode.class, 6);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/leftist.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		buildGraph();
		genTest0();
		genTest6();
		genTest3$3();
	}
	
	private static void genTest0() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH heap = specFty.mkRefDecl(LeftistHeap.class, "h");
		specFty.addRefSpec("h", "root", "null");
		specFty.setAccessible("h");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, heap);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest0: " + (end - start) + "ms\n");
	}
	
	private static void genTest6() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH heap = specFty.mkRefDecl(LeftistHeap.class, "h");
		ObjectH npl = specFty.mkVarDecl(SMTSort.INT, "npl");
		specFty.addRefSpec("h", "root", "o0");
		specFty.addRefSpec("o0", "left", "o1", "right", "o2", "npl", "npl");
		specFty.addRefSpec("o1", "left", "o3", "right", "o4");
		specFty.addRefSpec("o3", "left", "o5", "right", "null");
		specFty.addRefSpec("o2", "left", "null", "right", "null");
		specFty.addRefSpec("o4", "element", "v2");
		specFty.addRefSpec("o5", "element", "v5");
		specFty.addVarSpec("(= v2 v5)");
		specFty.setAccessible("h");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, heap, npl);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest6: " + (end - start) + "ms\n");
	}
	
	private static void genTest3$3() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH h1 = specFty.mkRefDecl(LeftistHeap.class, "h1");
		ObjectH h2 = specFty.mkRefDecl(LeftistHeap.class, "h2");
		specFty.addRefSpec("h1", "root", "o0");
		specFty.addRefSpec("o0", "left", "o1", "right", "o2");
		specFty.addRefSpec("o1", "left", "null", "right", "null", "element", "v1");
		specFty.addRefSpec("o2", "left", "null", "right", "null", "element", "v2");
		specFty.addRefSpec("h2", "root", "o3");
		specFty.addRefSpec("o3", "left", "o4", "right", "null", "element", "v3");
		specFty.addRefSpec("o4", "left", "o5", "right", "null");
		specFty.addRefSpec("o5", "left", "null", "right", "null");
		specFty.addVarSpec("(< v1 v3)");
		specFty.addVarSpec("(< v2 v3)");
		specFty.setAccessible("h1", "h2");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, h1, h2);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest3$3: " + (end - start) + "ms\n");
	}
	
}
