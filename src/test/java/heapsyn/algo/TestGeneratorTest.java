package heapsyn.algo;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import example.ListNode;
import example.ManualExecutor;
import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;
import heapsyn.wrapper.symbolic.PathDescriptor;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithJBSE;
import heapsyn.wrapper.symbolic.SymbolicHeapWithJBSE;

public class TestGeneratorTest {
	
	@SuppressWarnings("unused")
	static class BinInteger {
		private int value;
		public BinInteger makeDouble(boolean enable) {
			if (enable) this.value *= 2;
			return this;
		}
		public void makeInc() { this.value += 1; }
		public static BinInteger zero(BinInteger mustBeNull) {
			BinInteger x = new BinInteger(); return x;
		}
	}
	
	private static ClassH cBin;
	private static FieldH fVal;
	private static Method mDouble, mInc, mZero;
	private static ObjectH oNull;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cBin = ClassH.of(BinInteger.class);
		fVal = FieldH.of(BinInteger.class.getDeclaredField("value"));
		mDouble = BinInteger.class.getDeclaredMethod("makeDouble", boolean.class);
		mInc = BinInteger.class.getDeclaredMethod("makeInc");
		mZero = BinInteger.class.getDeclaredMethod("zero", BinInteger.class);
		oNull = ObjectH.NULL;
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test1() {
		BoolVar enable1 = new BoolVar(), enable2 = new BoolVar();
		IntVar x1 = new IntVar(), x2 = new IntVar();
		IntVar x3 = new IntVar(), x4 = new IntVar();
		ObjectH o1 = new ObjectH(cBin, ImmutableMap.of(fVal, new ObjectH(x1)));
		ObjectH o2 = new ObjectH(cBin, ImmutableMap.of(fVal, new ObjectH(x2)));
		ObjectH o3 = new ObjectH(cBin, ImmutableMap.of(fVal, new ObjectH(x3)));
		ObjectH o4 = new ObjectH(cBin, ImmutableMap.of(fVal, new ObjectH(x4)));
		
		SymbolicHeap emp = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		SymbolicHeap heap1 = new SymbolicHeapAsDigraph(
				Arrays.asList(o1, oNull), ExistExpr.ALWAYS_FALSE);
		SymbolicHeap heap2 = new SymbolicHeapAsDigraph(
				Arrays.asList(o2, oNull), ExistExpr.ALWAYS_FALSE);
		SymbolicHeap heap3 = new SymbolicHeapAsDigraph(
				Arrays.asList(o3, oNull), ExistExpr.ALWAYS_FALSE);
		SymbolicHeap heap4 = new SymbolicHeapAsDigraph(
				Arrays.asList(o4, oNull), ExistExpr.ALWAYS_FALSE);
		
		// H0 (empty heap)
		WrappedHeap H0 = new WrappedHeap(emp);
		
		// H0 => H1, invoke zero()
		MethodInvoke mInvoke1 = new MethodInvoke(mZero, Arrays.asList(oNull));
		PathDescriptor pd1 = new PathDescriptor();
		pd1.pathCond = null;
		pd1.retVal = o1;
		pd1.finHeap = heap1;
		pd1.objSrcMap = ImmutableMap.of();
		pd1.varExprMap = ImmutableMap.of(x1, new IntConst(0));
		WrappedHeap H1 = new WrappedHeap(H0, mInvoke1, pd1);
		
		// H1 => H2, invoke makeDouble(enable1), enable1 = true
		MethodInvoke mInvoke2 = new MethodInvoke(mDouble,
				Arrays.asList(o1, new ObjectH(enable1)));
		PathDescriptor pd2 = new PathDescriptor();
		pd2.pathCond = enable1;
		pd2.retVal = o2;
		pd2.finHeap = heap2;
		pd2.objSrcMap = ImmutableMap.of(o2, o1);
		pd2.varExprMap = ImmutableMap.of(x2,
				new ApplyExpr(SMTOperator.MUL, x1, new IntConst(2)));
		WrappedHeap H2 = new WrappedHeap(H1, mInvoke2, pd2);
		
		// H1 => H3, invoke makeDoule(enable2), enable2 = false
		MethodInvoke mInvoke3 = new MethodInvoke(mDouble,
				Arrays.asList(o1, new ObjectH(enable2)));
		PathDescriptor pd3 = new PathDescriptor();
		pd3.pathCond = new ApplyExpr(SMTOperator.UN_NOT, enable2);
		pd3.retVal = o3;
		pd3.finHeap = heap3;
		pd3.objSrcMap = ImmutableMap.of(o3, o1);
		pd3.varExprMap = ImmutableMap.of(x3, x1);
		WrappedHeap H3 = new WrappedHeap(H1, mInvoke3, pd3);
		
		// H1 => H4, invoke makeInc()
		MethodInvoke mInvoke4 = new MethodInvoke(mInc, Arrays.asList(o1));
		PathDescriptor pd4 = new PathDescriptor();
		pd4.pathCond = null;
		pd4.retVal = null;
		pd4.finHeap = heap4;
		pd4.objSrcMap = ImmutableMap.of(o4, o1);
		pd4.varExprMap = ImmutableMap.of(x4,
				new ApplyExpr(SMTOperator.ADD, x1, new IntConst(1)));
		WrappedHeap H4 = new WrappedHeap(H1, mInvoke4, pd4);
		
		// H2 ---> H1
		H1.subsumeHeap(H2, new Bijection<ObjectH, ObjectH>() {
			{ putUV(o2, o1); }
			{ putUV(o2.getFieldValue(fVal), o1.getFieldValue(fVal)); }
			{ putUV(oNull, oNull); }
		});
		
		// H3 ---> H1
		H1.subsumeHeap(H3, new Bijection<ObjectH, ObjectH>() {
			{ putUV(o3, o1); }
			{ putUV(o3.getFieldValue(fVal), o1.getFieldValue(fVal)); }
			{ putUV(oNull, oNull); }
		});
		
		// H4 ---> H1
		H1.subsumeHeap(H4, new Bijection<ObjectH, ObjectH>() {
			{ putUV(o4, o1); }
			{ putUV(o4.getFieldValue(fVal), o1.getFieldValue(fVal)); }
			{ putUV(oNull, oNull); }
		});
		
		for (int i = 0; i < 5; ++i) {
			H0.recomputeConstraint();
			H1.recomputeConstraint();
			H2.recomputeConstraint();
			H3.recomputeConstraint();
			H4.recomputeConstraint();
		}
	
		/*
		H0.__debugPrintOut(System.out);
		H1.__debugPrintOut(System.out);
		H2.__debugPrintOut(System.out);
		H3.__debugPrintOut(System.out);
		H4.__debugPrintOut(System.out);
		*/
		
		Specification spec = new Specification();
		IntVar x = new IntVar(), arg = new IntVar();
		ObjectH o = new ObjectH(cBin, ImmutableMap.of(fVal, new ObjectH(x)));
		spec.expcHeap = new SymbolicHeapAsDigraph(
				Arrays.asList(o, oNull), null);
		spec.condition = new ApplyExpr(SMTOperator.AND,
				new ApplyExpr(SMTOperator.BIN_EQ, x, arg),
				new ApplyExpr(SMTOperator.BIN_EQ, x, new IntConst(10)));
		
		TestGenerator testgen = new TestGenerator(Arrays.asList(H0, H1, H2, H3, H4));
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testgen.generateTestWithSpec(spec, objSrc, vModel);
		assertEquals("10", vModel.get(arg).toSMTString());
		stmts.add(new Statement(objSrc, vModel, o, new ObjectH(arg)));
		Statement.printStatements(stmts, System.out);
		System.out.println();
		System.out.flush();
	}
	
	private void makeTest(SymbolicExecutor executor, PrintStream ps) throws Exception {
		HeapTransGraphBuilder graphBuilder = new HeapTransGraphBuilder(
				executor,
				Arrays.asList(
						ListNode.mNew, ListNode.mGetNext,
						ListNode.mSetElem, ListNode.mGetElem,
						ListNode.mAddBefore, ListNode.mAddAfter
				)
		);
		SymbolicHeap initHeap = new SymbolicHeapWithJBSE(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> genHeaps = graphBuilder.buildGraph(initHeap);
		TestGenerator testgen = new TestGenerator(genHeaps);
		
		IntVar y = new IntVar();
		IntVar x1 = new IntVar(), x2 = new IntVar();
		IntVar x3 = new IntVar(), x4 = new IntVar();
		ObjectH o1 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x1), ListNode.fNext, ObjectH.NULL));
		ObjectH o2 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x2), ListNode.fNext, o1));
		ObjectH o3 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x3), ListNode.fNext, o2));
		ObjectH o4 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x4), ListNode.fNext, o2));
		Specification spec = new Specification();
		spec.expcHeap = new SymbolicHeapAsDigraph(
				Arrays.asList(o3, o4, o1, ObjectH.NULL), null);
		spec.condition = new ApplyExpr(SMTOperator.AND,
				new ApplyExpr(SMTOperator.BIN_EQ, x1, y),
				new ApplyExpr(SMTOperator.BIN_NE, x3, y),
				new ApplyExpr(SMTOperator.BIN_NE, x4, y));
		
		Map<ObjectH, ObjectH> objSrc = new HashMap<>();
		Map<Variable, Constant> vModel = new HashMap<>();
		List<Statement> stmts = testgen.generateTestWithSpec(spec, objSrc, vModel);
		stmts.add(new Statement(objSrc, vModel, o3, o4, o1, new ObjectH(y)));
		Statement.printStatements(stmts, ps);
		ps.flush();
	}
	
	@Test
	public void test2() throws Exception {
		makeTest(ManualExecutor.I(), new PrintStream("build/ManualTestgen.txt"));
	}
	
	// @Test
	public void test3() throws Exception {
		makeTest(new SymbolicExecutorWithJBSE(), new PrintStream("build/JBSETestgen.txt"));
	}
	
	@Test
	public void test4() throws Exception {
		makeTest(new SymbolicExecutorWithCachedJBSE(), new PrintStream("build/CachedJBSETestgen.txt"));
	}

}
