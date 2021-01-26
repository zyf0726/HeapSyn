package heapsyn.heap;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTSort;
import heapsyn.smtlib.Variable;

public class ObjectTest {
	
	@SuppressWarnings("unused")
	private class Node {
		public Node next;
		public int value;
	}
	
	private static ClassH cNode;
	private static FieldH fNext, fValue;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cNode = ClassH.of(Node.class);
		fNext = FieldH.of(Node.class.getDeclaredField("next"));
		fValue = FieldH.of(Node.class.getDeclaredField("value"));
	}
	
	@After
	public void tearDown() throws Exception {
		ObjectH.DEBUG_MODE = false;
	}

	@Test
	public void testClass1() {
		ClassH cNull = ClassH.of(), cNull_dup = ClassH.of();
		assertTrue(cNull == cNull_dup);
		
		assertNull(cNull.getJavaClass());
		assertNull(cNull.getSMTSort());
		assertTrue(cNull.isNullClass());
		assertFalse(cNull.isNonNullClass());
		assertTrue(cNull.isJavaClass());
		assertFalse(cNull.isSMTSort());
	}
	
	@Test
	public void testClass2() {
		ClassH cNode_dup = ClassH.of(Node.class);
		assertTrue(cNode == cNode_dup);
		assertNotEquals(ClassH.of(), cNode_dup);
		
		assertEquals(Node.class, cNode.getJavaClass());
		assertNull(cNode.getSMTSort());
		assertFalse(cNode.isNullClass());
		assertTrue(cNode.isNonNullClass());
		assertTrue(cNode.isJavaClass());
		assertFalse(cNode.isSMTSort());
	}
	
	@Test
	public void testClass3() {
		ClassH cBool = ClassH.of(SMTSort.BOOL);
		ClassH cInt = ClassH.of(SMTSort.INT);
		assertTrue(ClassH.of(SMTSort.BOOL) == cBool);
		assertEquals(cInt, ClassH.of(SMTSort.INT));
		assertNotEquals(cBool, cInt);
		
		assertNull(cBool.getJavaClass());
		assertEquals(SMTSort.BOOL, cBool.getSMTSort());
		assertEquals(SMTSort.INT, cInt.getSMTSort());
		assertFalse(cInt.isNullClass());
		assertFalse(cBool.isNonNullClass());
		assertFalse(cInt.isJavaClass());
		assertTrue(cBool.isSMTSort());
	}
	
	@Test(expected = NullPointerException.class)
	public void testClassExc1() {
		ClassH.of((Class<Node>) null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testClassExc2() {
		ClassH.of((SMTSort) null);
	}
	
	@Test
	public void testField() throws NoSuchFieldException {
		FieldH fNext_dup = FieldH.of(Node.class.getField("next"));
		FieldH fValue_dup = FieldH.of(Node.class.getField("value"));
		assertTrue(fNext == fNext_dup);
		assertEquals(fValue, fValue_dup);
		assertNotEquals(fNext, fValue);
		
		assertTrue(fValue.compareTo(fNext) / fNext.compareTo(fValue) < 0);
		assertTrue(fValue.compareTo(fValue) == 0);
		assertEquals("next", fNext.getName());
		assertEquals("value", fValue.getName());
	}
	
	@Test(expected = NullPointerException.class)
	public void testFieldExc1() {
		FieldH.of(null);
	}
	
	@Test
	public void testObject1() {
		ObjectH oNull = ObjectH.NULL;
		assertTrue(oNull.isNullObject());
		assertFalse(oNull.isNonNullObject());
		assertTrue(oNull.isHeapObject());
		assertFalse(oNull.isVariable());
		assertEquals(ClassH.of(), oNull.getClassH());
		assertNull(oNull.getVariable());
	}
	
	@Test
	public void testObject2() {
		Variable v1 = new IntVar(), v2 = new BoolVar();
		ObjectH ov1 = new ObjectH(v1), ov2 = new ObjectH(v2);
		assertFalse(ov1.isNullObject());
		assertFalse(ov2.isNonNullObject());
		assertFalse(ov1.isHeapObject());
		assertTrue(ov2.isVariable());
		assertEquals(ClassH.of(SMTSort.INT), ov1.getClassH());
		assertEquals(ClassH.of(SMTSort.BOOL), ov2.getClassH());
		assertEquals(v1, ov1.getVariable());
		assertEquals(v2, ov2.getVariable());
	}
	
	@Test
	public void testObject3() {
		ObjectH.DEBUG_MODE = true;
		Variable v1 = new IntVar(), v2 = new IntVar();
		ObjectH ov1 = new ObjectH(v1), ov2 = new ObjectH(v2);
		ObjectH o1 = new ObjectH(cNode, ImmutableMap.of());
		ObjectH o2 = new ObjectH(cNode, ImmutableMap.of(fNext, o1, fValue, ov2));
		assertFalse(o1.isNullObject());
		assertTrue(o2.isNonNullObject());
		assertTrue(o1.isHeapObject());
		assertFalse(o2.isVariable());
		assertEquals(cNode, o1.getClassH());
		assertNull(o2.getVariable());
		
		assertEquals(Collections.emptySet(), o1.getFields());
		assertEquals(Arrays.asList(o1, ov2), o2.getValues());
		assertEquals(ObjectH.NULL.getEntries(), o1.getEntries());
		o1.setFieldValueMap(ImmutableMap.of(fNext, o2, fValue, ov1));
		assertEquals(o1, o1.getFieldValue(fNext).getFieldValue(fNext));
		assertEquals(ov2, o2.getFieldValue(fValue));
	}
	
	@Test
	public void testObject4() {
		ObjectH o1 = new ObjectH(cNode, null);
		o1.setFieldValueMap(ImmutableMap.of(fNext, o1));
		ObjectH o2 = new ObjectH(cNode, ImmutableMap.of(fNext, o1));
		assertEquals(o1.getEntries(), o2.getEntries());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testObjectExc1() {
		ObjectH o = new ObjectH(cNode, ImmutableMap.of());
		o.setFieldValueMap(ImmutableMap.of());
	}
	
	@Test(expected = NullPointerException.class)
	public void testObjectExc2() {
		ObjectH o = new ObjectH(cNode, null);
		o.setFieldValueMap(null);
	}

}
