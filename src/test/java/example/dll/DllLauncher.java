package example.dll;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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

public class DllLauncher {

	private static TestGenerator testGenerator;
	
	private static void buildGraph() throws FileNotFoundException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("HexSettings/dll-accurate.jbse");
		parms.setHeapScope(LinkedList.class, 1);
		parms.setHeapScope(LinkedList.Entry.class, 4);
		parms.setHeapScope(LinkedList.ListItr.class, 1);
		parms.setHeapScope(LinkedList.DescendingIterator.class, 1);
		Set<Method> decMethods = ImmutableSet.copyOf(LinkedList.class.getDeclaredMethods());
		Set<Method> pubMethods = ImmutableSet.copyOf(LinkedList.class.getMethods());
		Set<Method> methods = Sets.intersection(decMethods, pubMethods);

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE(
				name -> !name.startsWith("_"));
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(LinkedList.class, 1);
		gb.setHeapScope(LinkedList.Entry.class, 4);
		gb.setHeapScope(LinkedList.ListItr.class, 1);
		gb.setHeapScope(LinkedList.DescendingIterator.class, 1);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap, true);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/jbse_dll.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		buildGraph();
		genTest1();
		genTest2();
		genTest3();
		genTest(0);
		genTest(2);
	}
	
	private static void genTest1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH dll = specFty.mkRefDecl(LinkedList.class, "dll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		specFty.addRefSpec("dll", "header", "o1", "size", "size");
		specFty.addRefSpec("o1", "next", "o2", "element", "null");
		specFty.addRefSpec("o2", "next", "o3", "element", "e1");
		specFty.addRefSpec("o3", "next", "o4", "element", "null");
		specFty.addRefSpec("o4", "next", "o1", "element", "e1");
		specFty.setAccessible("dll");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, dll, size);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
	}
	
	private static void genTest2() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH dll = specFty.mkRefDecl(LinkedList.class, "dll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		specFty.mkRefDecl(LinkedList.ListItr.class, "it");
		specFty.addRefSpec("dll", "header", "o1", "size", "size");
		specFty.addRefSpec("o1", "previous", "o0");
		specFty.addRefSpec("o0", "next", "o1");
		specFty.addRefSpec("it", "next", "o0");
		specFty.addVarSpec("(> size 2)");
		specFty.setAccessible("dll", "it");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, dll, size);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest2: " + (end - start) + "ms\n");
	}
	
	private static void genTest3() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH dll = specFty.mkRefDecl(LinkedList.class, "dll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		specFty.mkRefDecl(LinkedList.DescendingIterator.class, "dit");
		specFty.addRefSpec("dll", "header", "o1", "size", "size");
		specFty.addRefSpec("o1", "previous", "o0");
		specFty.addRefSpec("o0", "next", "o1");
		specFty.addRefSpec("dit", "itr", "it");
		specFty.addRefSpec("it", "next", "o1");
		specFty.addVarSpec("(> size 2)");
		specFty.setAccessible("dll", "dit");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, dll, size);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest3: " + (end - start) + "ms\n");
	}
	
	private static void genTest(int expcSize) {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH dll = specFty.mkRefDecl(LinkedList.class, "dll");
		ObjectH size = specFty.mkVarDecl(SMTSort.INT, "size");
		specFty.addRefSpec("dll", "size", "size");
		specFty.addVarSpec("(= size " + expcSize + ")");
		specFty.setAccessible("dll");
		Specification spec = specFty.genSpec();
		
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, dll, size);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest(" + expcSize + "): " + (end - start) + "ms\n");
	}
	
}
