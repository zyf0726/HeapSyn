package heapsyn.algo;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import heapsyn.algo.WrappedHeap.MatchResult;
import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.wrapper.symbolic.Specification;

import static heapsyn.smtlib.SMTOperator.*;

public class WrappedHeapTest2 {
	
	@SuppressWarnings("unused")
	static class Node {
		private int value;
		private Node next;
	}
	
	private static ClassH cNode;
	private static FieldH fValue, fNext;
	private static ObjectH oNull;
	
	static final int N = 20;
	private static IntVar[] iv;
	private static ObjectH[] ov;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cNode = ClassH.of(Node.class);
		fValue = FieldH.of(Node.class.getDeclaredField("value"));
		fNext = FieldH.of(Node.class.getDeclaredField("next"));
		oNull = ObjectH.NULL;
		iv = new IntVar[N];
		ov = new ObjectH[N];
		for (int i = 0; i < N; ++i) {
			iv[i] = new IntVar();
			ov[i] = new ObjectH(iv[i]);
		}
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test1() {
		ObjectH o1 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[1], fNext, oNull));
		ObjectH o2 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[2], fNext, o1));
		ObjectH o3 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[3], fNext, o2));
		ObjectH o4 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[4], fNext, o2));
		SMTExpression expr1 = new ApplyExpr(SMTOperator.AND,
				// o4.value = 2x + 1
				new ApplyExpr(BIN_EQ, iv[4],
						new ApplyExpr(ADD, new IntConst(1),
								new ApplyExpr(MUL, iv[0], new IntConst(2)))),
				// o3.value = 3x + 1
				new ApplyExpr(BIN_EQ, iv[3],
						new ApplyExpr(ADD, new IntConst(1),
								new ApplyExpr(MUL, iv[0], new IntConst(3)))),
				// o2.value = x - 1
				new ApplyExpr(BIN_EQ, iv[2],
						new ApplyExpr(SUB, iv[0], new IntConst(1))),
				// o1.value = x + 1
				new ApplyExpr(BIN_EQ, iv[1],
						new ApplyExpr(ADD, iv[0], new IntConst(1)))
		);
		SymbolicHeap supSymHeap = new SymbolicHeapAsDigraph(
				Arrays.asList(o2, o3, o4, oNull),
				new ExistExpr(Arrays.asList(iv[0]), expr1)
		);
		WrappedHeap supHeap = new WrappedHeap(supSymHeap); 
		
		ObjectH p1 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[5], fNext, oNull));
		ObjectH p2 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[6], fNext, p1));
		ObjectH p3 = new ObjectH(cNode, ImmutableMap.of(fValue, ov[7], fNext, p2));
		
		Specification spec1 = new Specification();
		spec1.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p2, oNull), null);
		// p1.value + p2.value = 4
		spec1.condition = new ApplyExpr(BIN_EQ, new IntConst(4),
				new ApplyExpr(ADD, iv[5], iv[6]));
		MatchResult ret1 = supHeap.matchSpecification(spec1);
		assertEquals(o2, ret1.objSrcMap.get(p2));
		assertNull(ret1.objSrcMap.get(p1));
		assertEquals("2", ret1.model.get(iv[0]).toSMTString()); // x = 2
		assertEquals("3", ret1.model.get(iv[1]).toSMTString()); // o1.value = 3
		assertEquals("1", ret1.model.get(iv[2]).toSMTString()); // o2.value = 1
		assertEquals("7", ret1.model.get(iv[3]).toSMTString()); // o3.value = 7
		assertEquals("5", ret1.model.get(iv[4]).toSMTString()); // o4.value = 5
		
		Specification spec2 = new Specification();
		spec2.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p2, oNull), null);
		// p2.value - p1.value = 2
		spec2.condition = new ApplyExpr(BIN_EQ, new IntConst(2),
				new ApplyExpr(SUB, iv[6], iv[5]));
		assertNull(supHeap.matchSpecification(spec2));
		
		Specification spec3 = new Specification();
		spec3.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p1, p2, oNull), null);
		// true
		spec3.condition = null;
		assertNull(supHeap.matchSpecification(spec3));
		
		Specification spec4 = new Specification();
		spec4.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p3, oNull), null);
		// p3.value = 4y + 2
		spec4.condition = new ApplyExpr(BIN_EQ, iv[7],
				new ApplyExpr(ADD, new IntConst(2),
						new ApplyExpr(MUL, iv[8], new IntConst(4))));
		MatchResult ret2 = supHeap.matchSpecification(spec4);
		assertEquals(o3, ret2.objSrcMap.get(p3));
		assertEquals(3 * Integer.parseInt(ret2.model.get(iv[0]).toSMTString()) + 1,
				4 * Integer.parseInt(ret2.model.get(iv[8]).toSMTString()) + 2);
		
		Specification spec5 = new Specification();
		spec5.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p2, p3, oNull), null);
		// p3.value = 6y + 3
		spec5.condition = new ApplyExpr(BIN_EQ, iv[7],
				new ApplyExpr(ADD, new IntConst(3),
						new ApplyExpr(MUL, iv[8], new IntConst(6))));
		MatchResult ret3 = supHeap.matchSpecification(spec5);
		assertEquals(o4, ret3.objSrcMap.get(p3));
		assertEquals(2 * Integer.parseInt(ret3.model.get(iv[0]).toSMTString()) + 1,
				6 * Integer.parseInt(ret3.model.get(iv[8]).toSMTString()) + 3);
		
		Specification spec6 = new Specification();
		spec6.expcHeap = new SymbolicHeapAsDigraph(Arrays.asList(p2, p3, oNull), null);
		// p3.value = 6y + 2
		spec6.condition = new ApplyExpr(BIN_EQ, iv[7],
				new ApplyExpr(ADD, new IntConst(2),
						new ApplyExpr(MUL, iv[8], new IntConst(6))));
		assertNull(supHeap.matchSpecification(spec6));
	}

}
