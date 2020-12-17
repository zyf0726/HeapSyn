package heapsyn;

import java.io.PrintStream;
import java.util.Arrays;

import examples.node.ManualExecutor;
import examples.node.Node;
import heapsyn.algo.HeapTransGraphBuilder;

public class Main {

	public static void main(String[] args) throws Exception {
		PrintStream ps = new PrintStream("D:/log.txt");
		System.setOut(ps);
		HeapTransGraphBuilder graphBuider = new HeapTransGraphBuilder(
				ManualExecutor.I(),
				Arrays.asList(
						Node.mNew,
						Node.mGetNext,
						Node.mGetElem,
						Node.mSetElem,
						Node.mAddAfter,
						Node.mAddBefore
				)
		);
		graphBuider.buildGraph();
		return;
	}

}
