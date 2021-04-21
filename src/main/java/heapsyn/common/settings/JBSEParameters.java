package heapsyn.common.settings;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import heapsyn.common.exceptions.UnexpectedInternalException;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;

public class JBSEParameters {
	
	private static final String Z3_PATH			=	"libs/z3.exe";
	private static final String JBSE_HOME		=	"jbse/";
	private static final String JBSE_CLASSPATH	= 	JBSE_HOME + "build/classes/java/main";
	private static final String JBSE_SOURCEPATH	=	JBSE_HOME + "src/main/java/";
	private static final String JRE_SOURCEPATH	=	System.getProperty("java.home", "") + "src.zip";
	
	private boolean doSignAnalysis			=	true;
	private boolean doEqualityAnalysis		=	true;
	private StateFormatMode stateFormatMode	=	StateFormatMode.TEXT;
	private StepShowMode stepShowMode		=	StepShowMode.LEAVES;
	
	
	private boolean showOnConsole	=	true;
	private String outFilePath		=	null;
	
	private Method targetMethod;
	private String targetClassPath;
	private String targetSourcePath;
	
	public void setShowOnConsole(boolean onConsole) {
		this.showOnConsole = onConsole;
	}
	
	public void setOutFilePath(String outFilePath) {
		this.outFilePath = outFilePath;
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
	
	
	public JBSEParameters() { }
	
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
