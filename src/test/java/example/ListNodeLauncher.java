package example;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import heapsyn.algo.HeapTransGraphBuilder;
import heapsyn.algo.Statement;
import heapsyn.algo.TestGenerator;
import heapsyn.algo.WrappedHeap;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.Variable;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithJBSE;
import heapsyn.wrapper.symbolic.SymbolicHeapWithJBSE;

import static heapsyn.smtlib.SMTOperator.*;

public class ListNodeLauncher {
	
	private static TestGenerator testGenerator;
	
	private static void buildGraph(Collection<Method> methods) {
		long start = System.currentTimeMillis();
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(
				new SymbolicExecutorWithJBSE(), methods);
		SymbolicHeap initHeap = new SymbolicHeapWithJBSE(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> heaps = gb.buildGraph(initHeap);
		testGenerator = new TestGenerator(heaps);		
		System.out.println("number of all heaps = " + heaps.size());
		System.out.println("number of symbolic execution = " + SymbolicExecutorWithJBSE.__countExecution);
		long end = System.currentTimeMillis();
		System.out.println(">> buildGraph: " + (end - start) + "ms\n");
	}
	
	private static void genTest1() {
		long start = System.currentTimeMillis();
		IntVar pv = new IntVar(), qv = new IntVar(), rv = new IntVar(), sv = new IntVar();
		ObjectH s = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(sv), ListNode.fNext, ObjectH.NULL));
		ObjectH r = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(rv), ListNode.fNext, s));
		ObjectH q = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(qv), ListNode.fNext, r));
		ObjectH p = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(pv), ListNode.fNext, q));
		Specification spec = new Specification();
		spec.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p, ObjectH.NULL), null);
		spec.condition = new ApplyExpr(AND,
				new ApplyExpr(BIN_NE, pv, new IntConst(0)),
				new ApplyExpr(BIN_NE, qv, new IntConst(0)),
				new ApplyExpr(BIN_EQ, rv, new ApplyExpr(ADD, pv, qv)),
				new ApplyExpr(BIN_EQ, sv, new ApplyExpr(ADD, qv, rv)));
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(Arrays.asList(objSrc.get(p)), Collections.emptyMap()));
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest1: " + (end - start) + "ms\n");
	}
	
	private static void genTest2() {
		long start = System.currentTimeMillis();
		IntVar[] vs = new IntVar[5];
		for (int i = 0; i < 5; ++i) {
			vs[i] = new IntVar();
		}
		ObjectH q = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(vs[0]), ListNode.fNext, ObjectH.NULL));
		ObjectH p = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(vs[0]), ListNode.fNext, q));
		ObjectH r = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(vs[0]), ListNode.fNext, p));
		ObjectH s = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(vs[0]), ListNode.fNext, p));
		ObjectH t = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(vs[0]), ListNode.fNext, q));
		Specification spec = new Specification();
		spec.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(r, s, t, ObjectH.NULL), null);
		spec.condition = null;
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testGenerator.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(
				Arrays.asList(objSrc.get(r), objSrc.get(s), objSrc.get(t)),
				Collections.emptyMap()));
		Statement.printStatements(stmts, System.out);
		long end = System.currentTimeMillis();
		System.out.println(">> genTest2: " + (end - start) + "ms\n");
	}
	
	public static void main(String[] args) {
		List<Method> methods = new ArrayList<>();
		methods.add(ListNode.mNew);
		methods.add(ListNode.mGetNext);
		methods.add(ListNode.mSetElem);
		methods.add(ListNode.mGetElem);
		methods.add(ListNode.mAddAfter);
		methods.add(ListNode.mAddBefore);
		buildGraph(methods);
		genTest1();
		genTest2();
	}

}
