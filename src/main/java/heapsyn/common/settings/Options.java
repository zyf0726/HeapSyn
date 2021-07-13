package heapsyn.common.settings;

import heapsyn.wrapper.smt.SMTSolver;
import heapsyn.wrapper.smt.Z3JavaAPI;

public class Options {
	
	private static Options INSTANCE = null;
	
	private Options() {
		this.smtSolver = new Z3JavaAPI();
	}
	
	public static Options I() {
		if (INSTANCE == null) {
			INSTANCE = new Options();
		}
		return INSTANCE;
	}
	
	// smt solver - z3
	private SMTSolver smtSolver;

	public SMTSolver getSMTSolver() {
		return this.smtSolver;
	}
	
	// target class path
	private String targetClassPath = "bin/test";
	
	public String getTargetClassPath() {
		return this.targetClassPath;
	}
	
	public void setTargetClassPath(String tcp) {
		this.targetClassPath=tcp;
	}
	
	// target source path
	private String targetSrcPath = "src/test/java";
	
	public String getTargetSourcePath() {
		return this.targetSrcPath;
	}
	
	public void setTargetSrcPath(String tsp) {
		this.targetSrcPath=tsp;
	}
	
	// maximum number of threads
	private int maxNumThreads = 1;
	
	public int getMaxNumThreads() {
		return this.maxNumThreads;
	}
	
}
