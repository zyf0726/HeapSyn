package heapsyn.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class BijectionTest {

	@Test
	public void testConstructor() {
		final int numObj = 15;
		Object[] objUs = new Object[numObj];
		Object[] objVs = new Object[numObj];
		for (int i = 0; i < numObj; ++i) {
			objUs[i] = new Object();
			objVs[i] = new Object();
		}
		
		Bijection<Object, Object> biject1 = new Bijection<>();		
		for (int i = 0; i < numObj; ++i) {
			assertFalse(biject1.containsU(objUs[i]));
			assertFalse(biject1.containsV(objVs[i]));
			assertNull(biject1.getV(objUs[i]));
			assertNull(biject1.getU(objVs[i]));
		}
		
		for (int i = 0; i < numObj; i += 3)
			assertTrue(biject1.putUV(objUs[i], objVs[i]));
		Bijection<Object, Object> biject2 = new Bijection<>(biject1);
		for (int i = 0; i < numObj; ++i) {
			assertEquals(biject1.containsU(objUs[i]), biject2.containsU(objUs[i]));
			assertEquals(biject1.containsV(objVs[i]), biject2.containsV(objVs[i]));
			assertEquals(biject1.getV(objUs[i]), biject2.getV(objUs[i]));
			assertEquals(biject1.getU(objVs[i]), biject2.getU(objVs[i]));
		}
		
		for (int i = 1; i < numObj; i += 3)
			assertTrue(biject2.putUV(objUs[i], objVs[i]));
		for (int i = 1; i < numObj; i += 3) {
			assertFalse(biject1.containsU(objUs[i]));
			assertNull(biject1.getU(objVs[i]));
			assertTrue(biject2.containsV(objVs[i]));
			assertEquals(objVs[i], biject2.getV(objUs[i]));
		}
		
		for (int i = 2; i < numObj; i += 3)
			assertTrue(biject1.putUV(objUs[i], objVs[i]));
		for (int i = 2; i < numObj; i += 3) {
			assertTrue(biject1.containsU(objUs[i]));
			assertEquals(objUs[i], biject1.getU(objVs[i]));
			assertFalse(biject2.containsV(objVs[i]));
			assertNull(biject2.getV(objUs[i]));
		}
	}
	
	@Test
	public void testFunctionality() {
		Object objA = new Object();
		Object objB = new Object();
		Object objC = new Object();
		Object objD = new Object();
		Bijection<Object, Object> B = new Bijection<>();
		
		assertTrue(B.putUV(objA, objA));
		assertTrue(B.putUV(objB, objC));
		assertEquals(objA, B.getV(objA));
		assertEquals(objA, B.getU(objA));
		assertEquals(objB, B.getU(objC));
		assertNull(B.getV(objC));
		assertEquals(2, B.size());
		
		assertFalse(B.putUV(objA, objD));
		assertFalse(B.putUV(objD, objC));
		assertFalse(B.containsU(objD));
		assertFalse(B.containsV(objD));
		assertEquals(2, B.size());
		
		assertTrue(B.putUV(objC, objD));
		assertEquals(objD, B.getV(objC));
		assertTrue(B.containsV(objD));
		assertTrue(B.putUV(objC, objD));
		assertEquals(3, B.size());
	}

}
