package heapsyn.util;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;

public class UniqueQueueTest {
	
	private static final int N = 10;
	private static Object[] objs;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		objs = new Object[N];
		for (int i = 0; i < N; ++i)
			objs[i] = new Object();
	}

	@Test(expected = NoSuchElementException.class)
	public void test1() {
		UniqueQueue<Object> uq = new UniqueQueue<>();
		for (int i = 0; i < N; ++i)
			assertTrue(uq.add(objs[i]));
		assertTrue(uq.contains(objs[2]));
		uq.clear();
		assertTrue(uq.isEmpty());
		assertEquals(0, uq.size());
		
		assertTrue(uq.add(objs[0]));
		assertFalse(uq.add(objs[0]));
		assertTrue(uq.add(objs[1]));
		assertFalse(uq.add(objs[0]));
		assertFalse(uq.isEmpty());
		assertEquals(2, uq.size());
		
		assertEquals(objs[0], uq.element());
		assertEquals(objs[0], uq.remove());
		assertFalse(uq.contains(objs[0]));
		
		assertTrue(uq.add(objs[0]));
		assertEquals(objs[1], uq.remove());
		assertEquals(objs[0], uq.remove());
		assertTrue(uq.isEmpty());
		
		uq.remove();
	}

}
