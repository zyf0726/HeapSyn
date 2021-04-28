package heapsyn.wrapper.symbolic;

import java.util.Collection;

import heapsyn.algo.MethodInvoke;
import heapsyn.heap.SymbolicHeap;

public interface SymbolicExecutor {
	
	public int getExecutionCount();
	
	public Collection<PathDescriptor> executeMethod(SymbolicHeap initHeap, MethodInvoke mInvoke);
	
	public Collection<PathDescriptor> executeMethodUnderTest(MethodInvoke mInvoke);

	
}
