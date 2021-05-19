package heapsyn.common.settings;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map.Entry;

import heapsyn.common.exceptions.LoadSettingsException;
import heapsyn.common.exceptions.UnexpectedInternalException;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.apps.settings.ParseException;
import jbse.apps.settings.SettingsReader;

public class JBSEParameters {
	
	private static final String Z3_PATH			=	"libs/z3-4.8.10-x64-win/z3.exe";
	private static final String JBSE_HOME		=	"jbse/";
	private static final String JBSE_CLASSPATH	= 	JBSE_HOME + "build/classes/java/main";
	private static final String JBSE_SOURCEPATH	=	JBSE_HOME + "src/main/java/";
	private static final String JRE_SOURCEPATH	=	System.getProperty("java.home", "") + "src.zip";
	
	private boolean doSignAnalysis			=	true;
	private boolean doEqualityAnalysis		=	true;
	private StateFormatMode stateFormatMode	=	StateFormatMode.PATH;
	private StepShowMode stepShowMode		=	StepShowMode.LEAVES;
	
	private boolean showOnConsole	=	false;
	private String outFilePath		=	null;
	private String settingsPath		=	null;
	
	private HashMap<String, Integer> heapScope = new HashMap<>();
	
	private Method targetMethod;
	private String targetClassPath;
	private String targetSourcePath;
	
	
	public void setShowOnConsole(boolean onConsole) {
		this.showOnConsole = onConsole;
	}
	
	public void setOutFilePath(String outFilePath) {
		this.outFilePath = outFilePath;
	}
	
	public void setSettingsPath(String settingsPath) {
		this.settingsPath = settingsPath;
	}
	
	public void setHeapScope(String className, int heapScope) {
		this.heapScope.put(className, heapScope);
	}
	
	public void setHeapScope(Class<?> javaClass, int heapScope) {
		this.heapScope.put(javaClass.getName().replace('.', '/'), heapScope);
	}
	
	public void setTargetMethod(Method targetMethod) {
		this.targetMethod = targetMethod;
	}
	
	public void setTargetClassPath(String targetClassPath) {
		this.targetClassPath = targetClassPath;
	}
	
	public void setTargetSourcePath(String targetSourcePath) {
		this.targetSourcePath = targetSourcePath;
	}
	
	
	private static JBSEParameters INSTANCE = null;
	
	private JBSEParameters() { }
	
	public static JBSEParameters I() {
		if (INSTANCE == null) {
			INSTANCE = new JBSEParameters();
		}
		return INSTANCE;
	}
	
	public RunParameters getRunParameters() {
		RunParameters rp = new RunParameters();
		rp.setJBSELibPath(JBSE_CLASSPATH);
		rp.addUserClasspath(this.targetClassPath);
		rp.addSourcePath(JBSE_SOURCEPATH, JRE_SOURCEPATH, this.targetSourcePath);
		rp.setMethodSignature(
				this.targetMethod.getDeclaringClass().getName().replace('.', '/'),
				getMethodSignature(this.targetMethod).replace('.', '/'),
				this.targetMethod.getName()
		);
		rp.setDecisionProcedureType(DecisionProcedureType.Z3);
		rp.setExternalDecisionProcedurePath(Z3_PATH);
		rp.setDoSignAnalysis(this.doSignAnalysis);
		rp.setDoEqualityAnalysis(this.doEqualityAnalysis);
		rp.setStateFormatMode(this.stateFormatMode);
		rp.setStepShowMode(this.stepShowMode);
		rp.setShowOnConsole(this.showOnConsole);
		if (this.outFilePath != null) {
			rp.setOutputFilePath(this.outFilePath);
		} else {
			rp.setOutputFileNone();
		}
		if (this.settingsPath != null) {
			try {
				SettingsReader sr = new SettingsReader(this.settingsPath);
				sr.fillRunParameters(rp);
			} catch (NoSuchFileException e) {
				throw new LoadSettingsException("settings file not found");
			} catch (ParseException e) {
				throw new LoadSettingsException("settings file syntactically ill-formed");
			} catch (IOException e) {
				throw new LoadSettingsException("error while closing settings file");
			}
		}
		for (Entry<String, Integer> entry : this.heapScope.entrySet()) {
			rp.setHeapScope(entry.getKey(), entry.getValue());
		}
		return rp;
	}
	
	// https://stackoverflow.com/questions/45072268/how-can-i-get-the-signature-field-of-java-reflection-method-object
	private static String getMethodSignature(Method m) {
		String sig;
		try {
			Field gSig = Method.class.getDeclaredField("signature");
			gSig.setAccessible(true);
			sig = (String) gSig.get(m);
			if (sig != null)
				return sig;
		} catch (IllegalAccessException | NoSuchFieldException e) {
			// this should never happen
			throw new UnexpectedInternalException(e);
		}

		StringBuilder sb = new StringBuilder("(");
		for (Class<?> c : m.getParameterTypes()) {
			sig = Array.newInstance(c, 0).toString();
			sb.append(sig.substring(1, sig.indexOf('@')));
		}
		sb.append(")");
		if (m.getReturnType() == void.class) {
			sb.append("V");
		} else {
			sig = Array.newInstance(m.getReturnType(), 0).toString();
			sb.append(sig.substring(1, sig.indexOf('@')));
		}
		return sb.toString();
	}
	
}
