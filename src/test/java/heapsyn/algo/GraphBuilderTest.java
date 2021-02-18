package heapsyn.algo;

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;

public class GraphBuilderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testListNode() throws Exception {
		PrintStream ps = new PrintStream("D:/log.txt");
		System.setOut(ps);
		HeapTransGraphBuilder graphBuilder = new HeapTransGraphBuilder(
				ManualExecutor.I(),
				Arrays.asList(
						ListNode.mNew, ListNode.mGetNext, ListNode.mSetElem,
						ListNode.mAddBefore, ListNode.mAddAfter
				)
		);
		List<WrappedHeap> heaps = graphBuilder.buildGraph(new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE));
		System.out.println("============================");
		System.out.println(heaps.size());
		for (int i = 0; i < heaps.size(); ++i) {
			System.out.println("i = " + i);
		 	heaps.get(i).__debugPrintOut(ps); 
		}
	}

}
