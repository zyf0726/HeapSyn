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

public class GraphBuilderIncrTest {

	private void makeTestListNode(SymbolicExecutor executor, PrintStream ps) throws Exception {
		HeapTransGraphBuilderIncr gbincr = new HeapTransGraphBuilderIncr(
				executor,
				Arrays.asList(
						ListNode.mNew, ListNode.mGetNext,
						ListNode.mSetElem, ListNode.mGetElem,
						ListNode.mAddBefore, ListNode.mAddAfter
				)
		);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> genHeaps = gbincr.buildGraph(initHeap, 6);
		HeapTransGraphBuilder.__debugPrintOut(genHeaps, executor, ps);
	}
	
	@Test
	public void testListNodeManual() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-Manual(Incr).log");
		makeTestListNode(ManualExecutor.I(), ps);
	}
	
	@Test
	public void testListNodeCachedJBSE() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-CachedJBSE(Incr).log");
		makeTestListNode(new SymbolicExecutorWithCachedJBSE(), ps);
	}

}
