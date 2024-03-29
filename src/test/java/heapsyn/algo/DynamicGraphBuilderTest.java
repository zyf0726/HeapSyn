package heapsyn.algo;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import example.ListNode;
import example.ManualExecutor;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class DynamicGraphBuilderTest {

	private void makeTestListNode(SymbolicExecutor executor, PrintStream ps) throws Exception {
		DynamicGraphBuilder gb = new DynamicGraphBuilder(
				executor,
				Arrays.asList(
						ListNode.mNew, ListNode.mGetNext,
						ListNode.mSetElem, ListNode.mGetElem,
						ListNode.mAddBefore, ListNode.mAddAfter
				)
		);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> genHeaps = gb.buildGraph(initHeap, 6);
		DynamicGraphBuilder.__debugPrintOut(genHeaps, executor, ps);
	}
	
	@Test
	public void testListNodeManual() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-Manual-Dynamic.log");
		makeTestListNode(ManualExecutor.I(), ps);
	}
	
	@Test
	public void testListNodeCachedJBSE() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-CachedJBSE-Dynamic.log");
		makeTestListNode(new SymbolicExecutorWithCachedJBSE(), ps);
	}

}
