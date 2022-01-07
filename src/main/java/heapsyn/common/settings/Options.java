package heapsyn.common.settings;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.wrapper.smt.ExternalSolver;
import heapsyn.wrapper.smt.SMTSolver;
import heapsyn.wrapper.smt.Z3JavaAPI;

public class Options {
	
	private static Options INSTANCE = null;
	
	private Options() {
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
		this.solverExecPath = this.homeDirPath.resolve("libs/z3-4.8.10-x64-win/z3.exe");
		this.solverTmpDir = this.homeDirPath.resolve("tmp");
		if (this.useExternalSolver) {
			this.smtSolver = new ExternalSolver(this.getSolverExecPath(), this.getSolverTmpDir());
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
	
	// home directory path
	private final Path homeDirPath;
	
	public Path getHomeDirectory() {
		return this.homeDirPath;
	}
	
	// smt solver configurations
	private Path solverExecPath;
	private Path solverTmpDir;
	private boolean useExternalSolver = false;
	private SMTSolver smtSolver;
	
	public void setSolverExecPath(String solverExecPath) {
		this.solverExecPath = Paths.get(solverExecPath);
	}
	
	public String getSolverExecPath() {
		return this.solverExecPath.toAbsolutePath().toString();
	}
	
	public void setSolverTmpDir(String solverTmpDir) {
		this.solverTmpDir = Paths.get(solverTmpDir);
	}
	
	public String getSolverTmpDir() {
		return this.solverTmpDir.toAbsolutePath().toString();
	}
	
	public void setUseExternalSolver(boolean useExternalSolver) {
		this.useExternalSolver = useExternalSolver;
	}
	
	public SMTSolver getSMTSolver() {
		return this.smtSolver;
	}
	
	// maximum number of threads (0 means multi-threading is disabled)
	private int maxNumThreads = 0;
	
	public int getMaxNumThreads() {
		return this.maxNumThreads;
	}
	
}
