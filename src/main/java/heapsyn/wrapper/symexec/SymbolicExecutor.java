package heapsyn.wrapper.symexec;

import java.util.Collection;

import heapsyn.algo.MethodInvoke;
import heapsyn.heap.SymbolicHeap;

public interface SymbolicExecutor {

	public Collection<PathDescriptor> executeMethod(SymbolicHeap initHeap, MethodInvoke mInvoke);
	
	public Collection<PathDescriptor> executeMethodUnderTest(SymbolicHeap heap, MethodInvoke mUnderTest);
	
}
