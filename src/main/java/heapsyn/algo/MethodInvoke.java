package heapsyn.algo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import heapsyn.heap.ObjectH;

public class MethodInvoke {

	private Method javaMethod;
	private ArrayList<ObjectH> invokeArgs;
	
	public MethodInvoke(Method javaMethod, Collection<ObjectH> invokeArgs) {
		this.javaMethod = javaMethod;
		if (invokeArgs == null) {
			this.invokeArgs = new ArrayList<>();
		} else {
			this.invokeArgs = new ArrayList<>(invokeArgs);
		}
	}
	
	public Method getJavaMethod() {
		return this.javaMethod;
	}
	
	public ArrayList<ObjectH> getInvokeArguments() {
		return new ArrayList<>(this.invokeArgs);
	}
	
}
