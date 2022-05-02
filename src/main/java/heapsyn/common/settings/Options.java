package heapsyn.common.settings;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.wrapper.smt.ExternalSolver;
import heapsyn.wrapper.smt.IncrSMTSolver;
import heapsyn.wrapper.smt.Z3JavaAPI;

public class Options {
	
	private static Options INSTANCE = null;
	
	private Options() { }
	
	public static Options I() {
		if (INSTANCE == null) {
			INSTANCE = new Options();
		}
		return INSTANCE;
	}
	
	// home directory path
	private Path homeDirPath = null;
	
	public Path getHomeDirectory() {
		if (this.homeDirPath == null) {
			try {
				/* (1) $HOME/bin/main/heapsyn/common/settings
				 * (2) $HOME/build/classes/java/main/heapsyn/common/settings
				 */
				File file = new File(Options.class.getResource("").toURI());
				if (file.getAbsolutePath().contains("build")) {
					for (int jump = 0; jump < 7; ++jump)
						file = file.getParentFile();
				} else {
					for (int jump = 0; jump < 5; ++jump)
						file = file.getParentFile();
				}
				this.homeDirPath = Paths.get(file.getAbsolutePath());
			} catch (URISyntaxException e) {
				throw new UnexpectedInternalException(e);
			}
		}
		return this.homeDirPath;
	}
	
	// smt solver configurations
	private Path solverExecPath = null;
	private Path solverTmpDir = null;
	private boolean useExternalSolver = false;
	private IncrSMTSolver smtSolver = null;
	
	public void setSolverExecPath(String solverExecPath) {
		this.solverExecPath = Paths.get(solverExecPath);
	}
	
	public String getSolverExecPath() {
		if (this.solverExecPath == null) {
			this.solverExecPath = this.homeDirPath.resolve("libs/z3-4.8.10-x64-win/z3.exe");
		}
		return this.solverExecPath.toAbsolutePath().toString();
	}
	
	public void setSolverTmpDir(String solverTmpDir) {
		this.solverTmpDir = Paths.get(solverTmpDir);
	}
	
	public String getSolverTmpDir() {
		if (this.solverTmpDir == null) {
			this.solverTmpDir = this.homeDirPath.resolve("tmp");
		}
		return this.solverTmpDir.toAbsolutePath().toString();
	}
	
	public void setUseExternalSolver(boolean useExternalSolver) {
		this.useExternalSolver = useExternalSolver;
	}
	
	public IncrSMTSolver getSMTSolver() {
		if (this.smtSolver == null) {
			if (this.useExternalSolver) {
				this.smtSolver = new ExternalSolver(this.getSolverExecPath(), this.getSolverTmpDir());
			} else {
				this.smtSolver = new Z3JavaAPI();
			}
		}
		return this.smtSolver;
	}
	
	// maximum number of threads (0 means multi-threading is disabled)
	private int maxNumThreads = 0;
	
	public int getMaxNumThreads() {
		return this.maxNumThreads;
	}
	
	// maximum time in seconds for building the graph
	private int budgetBuildGraph = 7200;
	
	public void setTimeBudget(int budgetBuildGraph) {
		this.budgetBuildGraph = budgetBuildGraph;
	}
	
	public int getTimeBudget() {
		return this.budgetBuildGraph;
	}
	
}
