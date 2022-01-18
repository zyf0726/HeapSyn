package heapsyn.algo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import example.ListNode;
import example.ManualExecutor;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTOperator;
import heapsyn.wrapper.symbolic.Specification;
import heapsyn.wrapper.symbolic.SymbolicExecutor;
import heapsyn.wrapper.symbolic.SymbolicExecutorWithCachedJBSE;

public class TestGeneratorTest2 {

	private void buildGraphForListNode(SymbolicExecutor executor,
			PrintStream ps, ObjectOutputStream oos) throws Exception {
		HeapTransGraphBuilder gb = new HeapTransGraphBuilder(
				executor,
				Arrays.asList(
						ListNode.mNew, ListNode.mGetNext,
						ListNode.mSetElem, ListNode.mGetElem,
						ListNode.mAddBefore, ListNode.mAddAfter
				)
		);
		SymbolicHeap initHeap = new SymbolicHeapAsDigraph(ExistExpr.ALWAYS_TRUE);
		List<WrappedHeap> genHeaps = gb.buildGraph(initHeap, false);
		HeapTransGraphBuilder.__debugPrintOut(genHeaps, executor, ps);
		oos.writeObject(genHeaps);
	}
	
	private List<WrappedHeap> buildWithManual() throws Exception {
		SymbolicExecutor executor = ManualExecutor.I();
		
		PrintStream psPre = new PrintStream("build/testListNode-Manual-pre.log");
		FileOutputStream fos = new FileOutputStream("build/testListNode-Manual.javaobj");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		buildGraphForListNode(executor, psPre, oos);
		psPre.close(); fos.close(); oos.close();
		
		FileInputStream fis = new FileInputStream("build/testListNode-Manual.javaobj");
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<WrappedHeap> genHeaps = ((List<?>) ois.readObject()).stream()
				.map(o -> (WrappedHeap) o).collect(Collectors.toList());
		fis.close(); ois.close();
		
		PrintStream psPost = new PrintStream("build/testListNode-Manual-post.log");
		HeapTransGraphBuilder.__debugPrintOut(genHeaps, executor, psPost);
		psPost.close();
		
		return genHeaps;
	}
	
	private List<WrappedHeap> buildWithJBSE() throws Exception {
		SymbolicExecutor executor = new SymbolicExecutorWithCachedJBSE();
		
		PrintStream psPre = new PrintStream("build/testListNode-CachedJBSE-pre.log");
		FileOutputStream fos = new FileOutputStream("build/testListNode-CachedJBSE.javaobj");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		buildGraphForListNode(executor, psPre, oos);
		psPre.close(); fos.close(); oos.close();
		
		FileInputStream fis = new FileInputStream("build/testListNode-CachedJBSE.javaobj");
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<WrappedHeap> genHeaps = ((List<?>) ois.readObject()).stream()
				.map(o -> (WrappedHeap) o).collect(Collectors.toList());
		fis.close(); ois.close();
		
		PrintStream psPost = new PrintStream("build/testListNode-CachedJBSE-post.log");
		HeapTransGraphBuilder.__debugPrintOut(genHeaps, executor, psPost);
		psPost.close();
		
		return genHeaps;
	}
	
	private void makeTest(List<WrappedHeap> genHeaps, PrintStream ps) throws Exception {
		IntVar y = new IntVar();
		IntVar x1 = new IntVar(), x2 = new IntVar();
		IntVar x3 = new IntVar(), x4 = new IntVar();
		ObjectH o1 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x1), ListNode.fNext, ObjectH.NULL));
		ObjectH o2 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x2), ListNode.fNext, o1));
		ObjectH o3 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x3), ListNode.fNext, o2));
		ObjectH o4 = new ObjectH(ListNode.classH,
				ImmutableMap.of(ListNode.fElem, new ObjectH(x4), ListNode.fNext, o2));
		Specification spec = new Specification();
		spec.expcHeap = new SymbolicHeapAsDigraph(
				Arrays.asList(o3, o4, o1, ObjectH.NULL), null);
		spec.condition = new ApplyExpr(SMTOperator.AND,
				new ApplyExpr(SMTOperator.BIN_EQ, x1, y),
				new ApplyExpr(SMTOperator.BIN_NE, x3, y),
				new ApplyExpr(SMTOperator.BIN_NE, x4, y));
		
		TestGenerator testgen = new TestGenerator(genHeaps);
		List<Statement> stmts = testgen.generateTestWithSpec(spec, o3, o4, o1, new ObjectH(y));
		Statement.printStatements(stmts, ps);
		ps.flush();
	}
	
	@Test
	public void testManual() throws Exception {
		List<WrappedHeap> genHeaps = buildWithManual();
		makeTest(genHeaps, new PrintStream("build/ManualTestgen.txt"));
	}
	
	@Test
	public void testCachedJBSE() throws Exception {
		List<WrappedHeap> genHeaps = buildWithJBSE();
		makeTest(genHeaps, new PrintStream("build/CachedJBSETestgen.txt"));
	}

}
