package example.kiasan.stackli;

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
import heapsyn.wrapper.symbolic.SpecFactory;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class StackLiLauncher {

	private static TestGenerator testGenerator;
	
	private static void buildGraph() throws NoSuchMethodException, FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("HexSettings/kiasan.jbse");
		parms.setHeapScope(StackLi.class, 1);
		parms.setHeapScope(ListNode.class, 6);
		List<Method> methods = new ArrayList<>();
		methods.add(StackLi.class.getMethod("__new__"));
		methods.add(StackLi.class.getMethod("isEmpty"));
		methods.add(StackLi.class.getMethod("isFull"));
		methods.add(StackLi.class.getMethod("makeEmpty"));
		methods.add(StackLi.class.getMethod("pop"));
		methods.add(StackLi.class.getMethod("push", Object.class));
		methods.add(StackLi.class.getMethod("top"));
		methods.add(StackLi.class.getMethod("topAndPop"));

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		StaticGraphBuilder gb = new StaticGraphBuilder(executor, methods);
		gb.setHeapScope(StackLi.class, 1);
		gb.setHeapScope(ListNode.class, 6);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap, true);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		StaticGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/stackli.txt"));
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
		ObjectH stack = specFty.mkRefDecl(StackLi.class, "stack");
		specFty.addRefSpec("stack", "topOfStack", "null");
		specFty.setAccessible("stack");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, stack);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTestEasy: " + (end - start) + "ms\n");
	}
	
	private static void genTestHard() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH stack = specFty.mkRefDecl(StackLi.class, "stack");
		specFty.addRefSpec("stack", "topOfStack", "s0");
		specFty.addRefSpec("s0", "next", "s1", "element", "e0");
		specFty.addRefSpec("s1", "next", "s2", "element", "e1");
		specFty.addRefSpec("s2", "next", "s3", "element", "e2");
		specFty.addRefSpec("s3", "next", "s4", "element", "e1");
		specFty.addRefSpec("s4", "next", "s5", "element", "null");
		specFty.addRefSpec("s5", "next", "null", "element", "e0");
		specFty.setAccessible("stack");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, stack);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTestHard: " + (end - start) + "ms\n");
	}
	
}
