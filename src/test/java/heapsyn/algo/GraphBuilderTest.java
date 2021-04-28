package heapsyn.algo;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import example.ListNode;
import example.ManualExecutor;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.ExistExpr;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;
import heapsyn.wrapper.symbolic.SymbolicHeapWithJBSE;

public class GraphBuilderTest {

	private void makeTestListNode(SymbolicExecutor executor, PrintStream ps) throws Exception {
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(
				executor,
				Arrays.asList(
						ListNode.mNew, ListNode.mGetNext,
						ListNode.mSetElem, ListNode.mGetElem,
						ListNode.mAddBefore, ListNode.mAddAfter
				)
		);
		SymbolicHeap initHeap = new SymbolicHeapWithJBSE(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> genHeaps = gb.buildGraph(initHeap);
		HeapTransGraphBuilder.__debugPrintOut(genHeaps, executor, ps);
	}
	
	@Test
	public void testListNodeManual() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-Manual.log");
		makeTestListNode(ManualExecutor.I(), ps);
	}
	
	@Test
	public void testListNodeCachedJBSE() throws Exception {
		PrintStream ps = new PrintStream("build/testListNode-CachedJBSE.log");
		makeTestListNode(new SymbolicExecutorWithCachedJBSE(), ps);
	}

}
