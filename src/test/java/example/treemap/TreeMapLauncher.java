package example.treemap;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

public class TreeMapLauncher {
	
	private static TestGenerator testGenerator;
	public List<Statement> stmts;
	
	public static void buildGraph() {
		JBSEParameters parms = JBSEParameters.I();
		parms.setShowOnConsole(true);
		parms.setSettingsPath("HexSettings/tree_map_accurate.jbse");
		parms.setHeapScope(TreeMap.class, 1);
		parms.setHeapScope(TreeMap.Entry.class, 4);
		parms.setDepthScope(500);
		parms.setCountScope(6000);
		List<Method> methods = new ArrayList<>();
		try {
			methods.add(TreeMap.class.getMethod("__new__"));
			//methods.add(TreeMap.class.getMethod("__ONew__"));
			methods.add(TreeMap.class.getMethod("put",int.class, Object.class));
			methods.add(TreeMap.class.getMethod("get",int.class));
			methods.add(TreeMap.class.getMethod("remove",int.class));
			methods.add(TreeMap.class.getMethod("clear"));
			methods.add(TreeMap.class.getMethod("size"));
			methods.add(TreeMap.class.getMethod("containsKey",int.class));
			methods.add(TreeMap.class.getMethod("containsValue",Object.class));
			methods.add(TreeMap.class.getMethod("firstKey"));
			methods.add(TreeMap.class.getMethod("lastKey"));

		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long start = System.currentTimeMillis();
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE(
				name -> !name.startsWith("_"));
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(executor, methods);
		gb.setHeapScope(TreeMap.class, 1);
		gb.setHeapScope(TreeMap.Entry.class, 4);
		//gb.setHeapScope(Object.class, 1);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + executor.getExecutionCount());
//		try {
//			HeapTransGraphBuilder.__debugPrintOut(heaps, executor, new PrintStream("treemap_obj.txt"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		testGenerator = new TestGenerator(heaps);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, NoSuchMethodException {
		
		buildGraph();
		genTest();
	}
	
	public static boolean genTest() {
		long start = System.currentTimeMillis();
		
		SpecFactory specFty = new SpecFactory();
		ObjectH treemap = specFty.mkRefDecl(TreeMap.class, "o5");
		//ObjectH o4=specFty.mkRefDecl(Object.class,"o3");
		specFty.addRefSpec("o5", "root", "o4");
		specFty.addRefSpec("o4",  "right", "o1", "parent", "null","left","o0");
		specFty.addRefSpec("o0", "parent", "o4", "left", "null","right","o2");
		specFty.addRefSpec("o1",  "parent", "o4");
		specFty.addRefSpec("o2",  "parent", "o0","left","null","right","null");
		//specFty.addRefSpec("o3");
		//specFty.setAccessible("o3");
		specFty.setAccessible("o5");
		
		Specification spec = specFty.genSpec();
		new WrappedHeap(spec.expcHeap).__debugPrintOut(System.out);
		//System.out.println(spec.condition.toSMTString());
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(objSrc, vModel, treemap));
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
		return true;
	}

}
