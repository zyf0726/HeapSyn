package heapsyn.common.settings;

import heapsyn.wrapper.smt.ExternalSolver;
import heapsyn.wrapper.smt.SMTSolver;
import heapsyn.wrapper.smt.Z3JavaAPI;

public class Options {
	
	private static Options INSTANCE = null;
	
	private Options() {
		if (this.useExternalSolver) {
			this.smtSolver = new ExternalSolver(solverExecPath, solverOutPath);
		} else {
			this.smtSolver = new Z3JavaAPI();
		}
	}
	
	public static Options I() {
		if (INSTANCE == null) {
			INSTANCE = new Options();
		}
		return INSTANCE;
	}
	
	// smt solver configurations
	private String solverExecPath = "libs/z3-4.8.10-x64-win/z3";
	private String solverOutPath = "tmp/temp.z3";
	private boolean useExternalSolver = false;
	private SMTSolver smtSolver;
	
	public void setUseExternalSolver(boolean useExternalSolver) {
		this.useExternalSolver = useExternalSolver;
	}
	
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
	
	// maximum number of threads (0 means multi-threading is disabled)
	private int maxNumThreads = 0;
	
	public int getMaxNumThreads() {
		return this.maxNumThreads;
	}
	
}
