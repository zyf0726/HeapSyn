package example.avl;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heapsyn.algo.HeapTransGraphBuilder;
import heapsyn.algo.Statement;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.common.settings.JBSEParameters;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.Variable;
import heapsyn.wrapper.symbolic.SpecFactory;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class AvlLauncher {
	
	private static TestGenerator testGenerator;
	
	private static void buildGraph(Collection<Method> methods) throws FileNotFoundException {
		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE(
				name -> !name.startsWith("_"));
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(AvlTree.class, 1);
		gb.setHeapScope(AvlNode.class, 6);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("tmp/avl.txt"));
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("HEXsettings/avltree.jbse");
		parms.setHeapScope(AvlTree.class, 1);
		parms.setHeapScope(AvlNode.class, 6);
		List<Method> methods = new ArrayList<>();
		methods.add(AvlTree.class.getMethod("__new__"));
		methods.add(AvlTree.class.getMethod("find", int.class));
		methods.add(AvlTree.class.getMethod("findMax"));
		methods.add(AvlTree.class.getMethod("findMin"));
		methods.add(AvlTree.class.getMethod("insertElem", int.class));
		methods.add(AvlTree.class.getMethod("isEmpty"));
		methods.add(AvlTree.class.getMethod("makeEmpty"));
		buildGraph(methods);
		genTest4$1();
		genTest4$2();
		genTest6$1();
	}
	
	private static void genTest4$1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH avlTree = specFty.mkRefDecl(AvlTree.class, "t");
		specFty.addRefSpec("t", "root", "root");
		specFty.addRefSpec("root", "element", "v2", "left", "root.l", "right", "root.r");
		specFty.addRefSpec("root.l", "element", "v1", "left", "root.l.l", "right", "null");
		specFty.addRefSpec("root.l.l", "element", "v0", "left", "null", "right", "null");
		specFty.addRefSpec("root.r", "element", "v3", "left", "null", "right", "null");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(objSrc, vModel, avlTree));
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest4$1: " + (end - start) + "ms\n");
	}
	
	private static void genTest4$2() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH avlTree = specFty.mkRefDecl(AvlTree.class, "t");
		specFty.addRefSpec("t", "root", "root");
		specFty.addRefSpec("root", "element", "v1", "left", "root.l", "right", "root.r");
		specFty.addRefSpec("root.l", "element", "v0", "left", "null", "right", "null");
		specFty.addRefSpec("root.r", "element", "v3", "left", "root.r.l", "right", "null");
		specFty.addRefSpec("root.r.l", "element", "v2", "left", "null", "right", "null");
		specFty.setAccessible("t");
		Specification spec = specFty.genSpec();
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(objSrc, vModel, avlTree));
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest4$2: " + (end - start) + "ms\n");
	}

	private static void genTest6$1() {
		long start = System.currentTimeMillis();
		SpecFactory specFty = new SpecFactory();
		ObjectH avlTree = specFty.mkRefDecl(AvlTree.class, "t");
		specFty.addRefSpec("t", "root", "root");
		specFty.addRefSpec("root", "element", "v3", "left", "root.l", "right", "root.r");
		specFty.addRefSpec("root.l", "element", "v1", "left", "root.l.l", "right", "root.l.r");
		specFty.addRefSpec("root.r", "element", "v4", "left", "null", "right", "root.r.r");
		specFty.addRefSpec("root.l.l", "element", "v0", "left", "null", "right", "null");
		specFty.addRefSpec("root.l.r", "element", "v2", "left", "null", "right", "null");
		specFty.addRefSpec("root.r.r", "element", "v5", "left", "null", "right", "null");
		specFty.setAccessible("t");
		specFty.addVarSpec("(= (- v1 v0) 27)"); // v0 = -115
		specFty.addVarSpec("(= (- v2 v1) 47)"); // v1 = -88
		specFty.addVarSpec("(= (- v3 v2) 36)"); // v2 = -41
		specFty.addVarSpec("(= (- v4 v3) 71)"); // v3 = -5
		specFty.addVarSpec("(= (- v5 v4) 62)"); // v4 = 66
		specFty.addVarSpec("(= (+ v0 v5) 13)"); // v5 = 128
		Specification spec = specFty.genSpec();
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(objSrc, vModel, avlTree));
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest6$1: " + (end - start) + "ms\n");
	}

}
