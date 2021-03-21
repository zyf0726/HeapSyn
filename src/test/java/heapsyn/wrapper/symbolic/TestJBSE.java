package heapsyn.wrapper.symbolic;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;

public class TestJBSE {
	
    //Customize them
    private static final String Z3_PATH           = "C:\\Program Files\\z3-4.8.9-x64-win\\bin\\z3.exe";
    private static final String JBSE_HOME         = "jbse/";

    //Leave them alone
    private static final String JBSE_CLASSPATH    = JBSE_HOME + "build/classes/java/main";
    private static final String JBSE_SOURCEPATH   = JBSE_HOME + "src/main/java/";
    private static final String TARGET_CLASSPATH  = "bin/test/";
    private static final String TARGET_SOURCEPATH = "src/test/java/";
    private static final String JRE_SOURCEPATH    = System.getProperty("java.home", "") + "src.zip";

    //Leave them alone, or add more stuff
    private static final String[] CLASSPATH       = { TARGET_CLASSPATH };
    private static final String[] SOURCEPATH      = { JBSE_SOURCEPATH, TARGET_SOURCEPATH, JRE_SOURCEPATH };

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		final RunParameters p = new RunParameters();
		set(p);
		final Run r = new Run(p);
		r.run();
	}
	
    private static final String METHOD_CLASS      = "example/ListNode"; 
    private static final String METHOD_DESCRIPTOR = "(I)Z"; 
    private static final String METHOD_NAME       = "setElem";
	
    private static void set(RunParameters p) {
        p.setJBSELibPath(JBSE_CLASSPATH);
        p.addUserClasspath(CLASSPATH);
        p.addSourcePath(SOURCEPATH);
        p.setMethodSignature(METHOD_CLASS, METHOD_DESCRIPTOR, METHOD_NAME);
        // p.setOutputFilePath("TestJBSE.txt");
        p.setDecisionProcedureType(DecisionProcedureType.Z3);
        p.setExternalDecisionProcedurePath(Z3_PATH);
        p.setStateFormatMode(StateFormatMode.JUNIT_TEST);
        p.setStepShowMode(StepShowMode.LEAVES);
        p.setShowWarnings(false); 
    }

}
