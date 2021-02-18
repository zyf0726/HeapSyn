package heapsyn.common.settings;

import java.lang.reflect.Method;
import java.util.List;

import heapsyn.wrapper.symbolic.SymbolicExecutor;

public class Options {
	
	private static Options INSTANCE = null;
	
	public Options I() {
		if (INSTANCE == null) {
			INSTANCE = new Options();
		}
		return INSTANCE;
	}

	// symbolic executor - JBSE
	private SymbolicExecutor symExec;
	
	// candidate (public) methods
	private List<Method> candiMethods;
	
	// method under test
	private Method testMethod;
	
	
	public SymbolicExecutor getSymbolicExecutor() {
		return this.symExec;
	}
	
	public List<Method> getCandidateMethods() {
		return this.candiMethods;
	}
	
	public Method getMethodUnderTest() {
		return this.testMethod;
	}
	
}
