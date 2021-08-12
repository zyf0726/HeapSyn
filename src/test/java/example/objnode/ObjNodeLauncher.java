package example.objnode;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import heapsyn.algo.HeapTransGraphBuilder;
import heapsyn.algo.Statement;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SpecFactory;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class ObjNodeLauncher {
	
	private static TestGenerator buildGraph(Collection<Method> methods, String outfile)
			throws FileNotFoundException {
		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(ObjNode.class, 3);
		gb.setHeapScope(Object.class, 5);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		if (outfile != null) {
			HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream(outfile));
		}
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
				ObjNode.mAddBefore, ObjNode.mAddAfter, ObjNode.mAddAfterFresh,
				ObjNode.mSetValueAliasNext, ObjNode.mMakeValueFresh
		);
		buildGraph(allMethods, "tmp/objnode.txt");
		genTest1();
		genTest2();
		genTest3();
		genTest4();
	}
	
	public static void genTest1() throws FileNotFoundException {
		SpecFactory specFty = new SpecFactory();
		ObjectH o1 = specFty.mkRefDecl(ObjNode.class, "o1");
		ObjectH v2 = specFty.mkRefDecl(Object.class, "v2");
		specFty.addRefSpec("o1", "nxt", "o2", "val", "v1");
		specFty.addRefSpec("o2", "nxt", "o3", "val", "v2");
		specFty.addRefSpec("o3", "nxt", "null", "val", "v2");
		specFty.setAccessible("o1", "v2");
		Specification spec = specFty.genSpec();
		TestGenerator testgen = buildGraph(
				Lists.newArrayList(
						ObjNode.mNewAlias, ObjNode.mNewNull, ObjNode.mNewFresh,
						ObjNode.mAddBefore, ObjNode.mAddAfter, ObjNode.mSetValueAliasNext),
				null);
		
		long start = System.currentTimeMillis();
		List<Statement> stmts = testgen.generateTestWithSpec(spec, o1, v2);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
	}
	
	public static void genTest2() throws FileNotFoundException {
		SpecFactory specFty = new SpecFactory();
		ObjectH o1 = specFty.mkRefDecl(ObjNode.class, "o1");
		ObjectH o2 = specFty.mkRefDecl(ObjNode.class, "o2");
		ObjectH o3 = specFty.mkRefDecl(ObjNode.class, "o3");
		ObjectH v1 = specFty.mkRefDecl(Object.class, "v1");
		ObjectH v2 = specFty.mkRefDecl(Object.class, "v2");
		specFty.addRefSpec("o1", "nxt", "o2", "val", "v1");
		specFty.addRefSpec("o2", "nxt", "o3", "val", "v1");
		specFty.addRefSpec("o3", "nxt", "null", "val", "v2");
		specFty.setAccessible("o1", "o2", "o3", "v1", "v2");
		Specification spec = specFty.genSpec();
		TestGenerator testgen = buildGraph(
				Lists.newArrayList(
						ObjNode.mNewNull, ObjNode.mAddAfterFresh,
						ObjNode.mGetNext, ObjNode.mGetValue,
						ObjNode.mSetFreshValue, ObjNode.mMakeValueFresh),
				null);
		
		long start = System.currentTimeMillis();
		List<Statement> stmts = testgen.generateTestWithSpec(spec, o1, v1, o2, v1, o3, v2);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest2: " + (end - start) + "ms\n");
	}

	public static void genTest3() throws FileNotFoundException {
		SpecFactory specFty = new SpecFactory();
		ObjectH o1 = specFty.mkRefDecl(ObjNode.class, "o1");
		ObjectH v = specFty.mkRefDecl(Object.class, "v");
		specFty.addRefSpec("o1", "nxt", "o2", "val", "v");
		specFty.addRefSpec("o2", "nxt", "null", "val", "v");
		specFty.setAccessible("o1", "v");
		Specification spec = specFty.genSpec();
		TestGenerator testgen = buildGraph(
				Lists.newArrayList(
						ObjNode.mNewFresh, ObjNode.mAddAfterFresh,
						ObjNode.mGetValue, ObjNode.mSetValueAliasNext),
				null);
		
		long start = System.currentTimeMillis();
		List<Statement> stmts = testgen.generateTestWithSpec(spec, o1, v);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest3: " + (end - start) + "ms\n");
	}
	
	public static void genTest4() throws FileNotFoundException {
		SpecFactory specFty = new SpecFactory();
		ObjectH o1 = specFty.mkRefDecl(ObjNode.class, "o1");
		ObjectH o3 = specFty.mkRefDecl(ObjNode.class, "o3");
		specFty.addRefSpec("o1", "nxt", "o2", "val", "v");
		specFty.addRefSpec("o2", "nxt", "o3", "val", "null");
		specFty.addRefSpec("o3", "nxt", "null", "val", "null");
		specFty.setAccessible("o1", "o3", "v");
		Specification spec = specFty.genSpec();
		TestGenerator testgen = buildGraph(
				Lists.newArrayList(
						ObjNode.mNewFresh, ObjNode.mResetValue,
						ObjNode.mAddBefore, ObjNode.mAddAfter, ObjNode.mGetValue),
				null);
		
		long start = System.currentTimeMillis();
		List<Statement> stmts = testgen.generateTestWithSpec(spec, o1, o3);
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest4: " + (end - start) + "ms\n");
	}
	
}
