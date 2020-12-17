package heapsyn.heap;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import heapsyn.smtlib.ExistentialProposition;
import heapsyn.smtlib.IntVariable;
import heapsyn.util.Bijection;

public class SymbolicHeapTest {
	
	@SuppressWarnings("unused")
	private class Node {
		private Node next;
		private int value;
	}
	
	private static ClassH clsNode;
	private static FieldH fNext, fValue;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		clsNode = ClassH.I(Node.class);
		fNext = FieldH.I(Node.class.getDeclaredField("next"));
		fValue = FieldH.I(Node.class.getDeclaredField("value"));
	}


	@SuppressWarnings("serial")
	@Test(expected = IllegalArgumentException.class)
	public void testAboutIsomorphism1() {
		SymbolicHeap emp = new SymbolicHeap();
		
		ObjectH o1$h1 = new ObjectH(clsNode, null);
		ObjectH o2$h1 = new ObjectH(clsNode, null);
		ObjectH o3$h1 = new ObjectH(clsNode, null);
		ObjectH v1$h1 = new ObjectH(new IntVariable());
		ObjectH v2$h1 = new ObjectH(new IntVariable());
		ObjectH v3$h1 = new ObjectH(new IntVariable());
		o1$h1.setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(fNext, o3$h1); put(fValue, v1$h1); }
		});
		o2$h1.setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(fNext, o3$h1); put(fValue, v2$h1); }
		});
		o3$h1.setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(fNext, ObjectH.NULL_OBJECT); put(fValue, v3$h1); }
		});
		SymbolicHeap h1 = new SymbolicHeap(Arrays.asList(o3$h1, o1$h1, o2$h1, ObjectH.NULL_OBJECT), null);
		
		ObjectH o1$h2 = new ObjectH(clsNode, null);
		ObjectH o2$h2 = new ObjectH(clsNode, null);
		ObjectH o3$h2 = new ObjectH(clsNode, null);
		ObjectH v1$h2 = new ObjectH(new IntVariable());
		ObjectH v2$h2 = new ObjectH(new IntVariable());
		ObjectH v3$h2 = new ObjectH(new IntVariable());
		o3$h2.setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(fNext, o1$h2); put(fValue, v3$h2); }
		});
		o2$h2.setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(fNext, o1$h2); put(fValue, v2$h2); }
		});
		o1$h2.setFieldValueMap(new HashMap<FieldH, ObjectH>() {
			{ put(fNext, ObjectH.NULL_OBJECT); put(fValue, v1$h2); }
		});
		SymbolicHeap h2 = new SymbolicHeap(
				Arrays.asList(o3$h2, o2$h2, o1$h2, ObjectH.NULL_OBJECT),
				ExistentialProposition.ALWAYS_TRUE
		);
		SymbolicHeap h3 = new SymbolicHeap(
				Arrays.asList(o3$h2, o1$h2, ObjectH.NULL_OBJECT),
				null
		);
		
		assertTrue(h1.maybeIsomorphic(h2));
		assertFalse(h1.maybeIsomorphic(h3));
		Bijection<ObjectH, ObjectH> isoMap = h1.getIsomorphicMapping(h2);
		assertNotNull(isoMap);
		assertEquals(7, isoMap.size());
		assertEquals(o1$h2, isoMap.getV(o3$h1));
		assertTrue(isoMap.getU(o2$h2) == o1$h1 || isoMap.getU(o2$h2) == o2$h1);
		assertTrue(isoMap.getU(o3$h2) == o1$h1 || isoMap.getU(o3$h2) == o2$h1);
		assertEquals(v1$h2, isoMap.getV(v3$h1));
		
		assertFalse(emp.maybeIsomorphic(h1));
		assertNull(h1.getIsomorphicMapping(emp));
		
		new SymbolicHeap(Arrays.asList(o1$h1), null);
	}
	
	@SuppressWarnings("serial")
	public void testAboutIsomorphism2() {
		ObjectH[] objsA = new ObjectH[11];
		ObjectH[] objsB = new ObjectH[11];
		for (int i = 0; i < 10; ++i) {
			objsA[i] = new ObjectH(clsNode, null);
			objsB[i] = new ObjectH(clsNode, null);
		}
		objsA[10] = objsB[10] = ObjectH.NULL_OBJECT;
		for (int i = 0; i < 10; ++i) {
			ObjectH nxtObjA = objsA[(i + 1) % 10];
			ObjectH nxtObjB = objsB[(i + 9) % 10];
			objsA[i].setFieldValueMap(new HashMap<FieldH, ObjectH>() {
				{ put(fNext, nxtObjA); }
			});
			objsB[i].setFieldValueMap(new HashMap<FieldH, ObjectH>() {
				{ put(fNext, nxtObjB); }
			});
		}
		
		SymbolicHeap hA = new SymbolicHeap(Arrays.asList(objsA), null);
		SymbolicHeap hB = new SymbolicHeap(Arrays.asList(objsB), null);
		assertTrue(hA.maybeIsomorphic(hB));
		
		Bijection<ObjectH, ObjectH> isoMap = hA.getIsomorphicMapping(hB);
		assertNotNull(isoMap);
		for (int i = 0; i < 10; ++i) {
			ObjectH o0$a2b = isoMap.getV(objsA[0]);
			ObjectH oi$a2b = isoMap.getV(objsA[i]);
			assertEquals((0 + Arrays.asList(objsB).indexOf(o0$a2b)) % 10,
						 (i + Arrays.asList(objsB).indexOf(oi$a2b)) % 10);
		}
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testAboutGetAll() throws Exception {
		@SuppressWarnings("unused")
		class B {
			private int i;
			private long l;
		}
		@SuppressWarnings("unused")
		class A {
			private int i;
			private B o;
		}
		
		ClassH cA = ClassH.I(A.class);
		ClassH cB = ClassH.I(B.class);
		FieldH AfI = FieldH.I(A.class.getDeclaredField("i"));
		FieldH AfO = FieldH.I(A.class.getDeclaredField("o"));
		FieldH BfI = FieldH.I(B.class.getDeclaredField("i"));
		FieldH BfL = FieldH.I(B.class.getDeclaredField("l"));
		IntVariable v1 = new IntVariable();
		IntVariable v2 = new IntVariable();
		IntVariable v3 = new IntVariable();
		
		ObjectH o2 = new ObjectH(cB, new HashMap<FieldH, ObjectH>() {
			{ put(BfI, new ObjectH(v2)); put(BfL, new ObjectH(v3)); }
		});
		ObjectH o1 = new ObjectH(cA, new HashMap<FieldH, ObjectH>() {
			{ put(AfO, o2); put(AfI, new ObjectH(v1)); }
		});
		
		SymbolicHeap h = new SymbolicHeap(Arrays.asList(o1, ObjectH.NULL_OBJECT), null);
		assertEquals(3, h.getAllVariables().size());
		assertTrue(h.getAllVariables().contains(v1));
		assertTrue(h.getAllVariables().contains(v2));
		assertTrue(h.getAllVariables().contains(v3));
		assertEquals(6, h.getAllObjects().size());
		assertTrue(h.getAllObjects().contains(o2));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testCloneObjects() {
		ObjectH v1 = new ObjectH(new IntVariable());
		ObjectH v2 = new ObjectH(new IntVariable());
		ObjectH v3 = new ObjectH(new IntVariable());
		ObjectH o3 = new ObjectH(clsNode, new HashMap<FieldH, ObjectH>() {
			{ put(fNext, ObjectH.NULL_OBJECT); put(fValue, v3); }
		});
		ObjectH o2 = new ObjectH(clsNode, new HashMap<FieldH, ObjectH>() {
			{ put(fNext, o3); put(fValue, v2); }
		});
		ObjectH o1 = new ObjectH(clsNode, new HashMap<FieldH, ObjectH>() {
			{ put(fNext, o2); put(fValue, v1); }
		});
		
		SymbolicHeap h = new SymbolicHeap(Arrays.asList(o1, ObjectH.NULL_OBJECT), null);
		Bijection<ObjectH, ObjectH> cloneMap = new Bijection<>();
		Collection<ObjectH> objClones = h.cloneObjects(cloneMap);
		
		assertEquals(h.getAllObjects().size(), objClones.size());
		assertEquals(cloneMap.getV(o2), cloneMap.getV(o1).getValue(fNext));
		assertEquals(ObjectH.NULL_OBJECT, cloneMap.getV(o3).getValue(fNext));
		assertEquals(cloneMap.getV(v2), cloneMap.getV(o2).getValue(fValue));
		
		assertNotEquals(cloneMap.getV(v1).getVariable(), v1.getVariable());
		assertEquals(v1.getVariable().getSMTSort(),
				cloneMap.getV(v1).getVariable().getSMTSort());
	}

}
