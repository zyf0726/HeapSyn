package heapsyn.algo;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import example.ListNode;
import example.ManualExecutor;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithJBSE;
import heapsyn.wrapper.symbolic.SymbolicHeapWithJBSE;

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

	private void makeTestListNode(SymbolicExecutor executor, PrintStream ps) throws Exception {
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
		ps.println("number of all heaps = " + genHeaps.size());
		int countNotSub = 0;
		for (WrappedHeap heap : genHeaps)
			if (!heap.getStatus().equals(HeapStatus.SUBSUMED))
				++countNotSub;
		ps.println("number of heaps not subsumed = " + countNotSub);
		int countTrans = 0;
		for (WrappedHeap heap : genHeaps) {
			countTrans += heap.getBackwardRecords().size();
		}
		ps.println("number of transitions = " + countTrans);
		ps.print("number of symbolic execution = ");
		if (executor instanceof ManualExecutor) {
			ps.println(ManualExecutor.I().__countExecution);
		} else if (executor instanceof SymbolicExecutorWithJBSE) {
			ps.println(SymbolicExecutorWithJBSE.__countExecution);
		}
		else {
			ps.println(SymbolicExecutorWithCachedJBSE.__countExecution);
		}
		ps.println();
		for (WrappedHeap heap : genHeaps)
			heap.__debugPrintOut(ps);
	}
	
	@Test
	public void testListNodeManual() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-Manual.log");
		makeTestListNode(ManualExecutor.I(), ps);
	}
	
	// @Test
	public void testListNodeJBSE() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-JBSE.log");
		makeTestListNode(new SymbolicExecutorWithJBSE(), ps);
	}
	
	@Test
	public void testListNodeCachedJBSE() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-CachedJBSE.log");
		makeTestListNode(new SymbolicExecutorWithCachedJBSE(), ps);
	}

}
