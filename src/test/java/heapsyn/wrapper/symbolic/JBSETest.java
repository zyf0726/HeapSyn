package heapsyn.wrapper.symbolic;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.apps.settings.ParseException;
import jbse.apps.settings.SettingsReader;

public class JBSETest {
	
    //Customize them
    private static final String Z3_PATH           = "libs/z3-4.8.10-x64-win/z3.exe";
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
    
    private static void makeTest(String mClass, String mDesc, String mName,
    		String outPath, String hexFilePath,
    		Map<String, Integer> heapScope) {
		final RunParameters p = new RunParameters();
        p.setJBSELibPath(JBSE_CLASSPATH);
        p.addUserClasspath(CLASSPATH);
        p.addSourcePath(SOURCEPATH);
        p.setMethodSignature(mClass, mDesc, mName);
        p.setDecisionProcedureType(DecisionProcedureType.Z3);
        p.setExternalDecisionProcedurePath(Z3_PATH);
        p.setStateFormatMode(StateFormatMode.TEXT);
        p.setStepShowMode(StepShowMode.LEAVES);
        p.setShowWarnings(false);
        p.setShowOnConsole(false);
        
        if (outPath != null) {
        	p.setOutputFilePath(outPath);
        } else {
        	p.setOutputFileNone();
        }
        
        if (heapScope != null) {
        	for (Entry<String, Integer> entry : heapScope.entrySet())
        		p.setHeapScope(entry.getKey(), entry.getValue());
        }
        
        if (hexFilePath != null) {
			try {
				new SettingsReader(hexFilePath).fillRunParameters(p);
			} catch (NoSuchFileException e) {
				System.err.println("Error: settings file not found.");
				System.exit(1);
			} catch (ParseException e) {
				System.err.println("Error: settings file syntactically ill-formed.");
				System.exit(2);
			} catch (IOException e) {
				System.err.println("Error while closing settings file.");
				System.exit(2);
			}
        }
        
		final Run r = new Run(p);
		r.run();
    }

	@Test
	public void testListNode() {
    	final String METHOD_CLASS		= "example/ListNode"; 
    	final String METHOD_DESCRIPTOR	= "(I)Z"; 
    	final String METHOD_NAME		= "setElem";
    	final String OUTPUT_FILE_PATH	= "tmp/JBSETest-ListNode.txt";
    	
		makeTest(METHOD_CLASS, METHOD_DESCRIPTOR, METHOD_NAME,
				OUTPUT_FILE_PATH, null, null);
	}
	
	@Test
	public void testAATree() {
    	final String METHOD_CLASS		= "example/kiasan/aatree/AATree"; 
    	final String METHOD_DESCRIPTOR	= "(I)V"; 
    	final String METHOD_NAME		= "remove";
    	final String OUTPUT_FILE_PATH	= "tmp/JBSETest-AATree.txt";
    	final String SETTINGS_FILE		= "HEXsettings/kiasan.jbse";
    	
		makeTest(METHOD_CLASS, METHOD_DESCRIPTOR, METHOD_NAME,
				OUTPUT_FILE_PATH, SETTINGS_FILE,
				ImmutableMap.of("example/kiasan/aatree/AATree$AANode", 4));
	}
    
	@Test
	public void testBST() {
    	final String METHOD_CLASS		= "example/kiasan/bst/BinarySearchTree"; 
    	final String METHOD_DESCRIPTOR	= "(I)V"; 
    	final String METHOD_NAME		= "remove";
    	final String OUTPUT_FILE_PATH	= "tmp/JBSETest-BST.txt";
    	final String SETTINGS_FILE		= "HEXsettings/kiasan.jbse";
    	
    	makeTest(METHOD_CLASS, METHOD_DESCRIPTOR, METHOD_NAME,
    			OUTPUT_FILE_PATH, SETTINGS_FILE,
    			ImmutableMap.of("example/kiasan/bst/BinaryNode", 5));
	}
    
	@Test
	public void testLeftist() {
    	final String METHOD_CLASS		= "example/kiasan/leftist/LeftistHeap"; 
    	final String METHOD_DESCRIPTOR	= "(Lexample/kiasan/leftist/LeftistHeap;)V"; 
    	final String METHOD_NAME		= "merge";
    	final String OUTPUT_FILE_PATH	= "tmp/JBSETest-Leftist.txt";
    	final String SETTINGS_FILE		= "HEXsettings/kiasan.jbse";
    	
		makeTest(METHOD_CLASS, METHOD_DESCRIPTOR, METHOD_NAME,
				OUTPUT_FILE_PATH, SETTINGS_FILE,
				ImmutableMap.of("example/kiasan/leftist/LeftistHeap$LeftistNode", 6));
	}
	
	@Test
	public void testStackLi() {
    	final String METHOD_CLASS		= "example/kiasan/stackli/StackLi"; 
    	final String METHOD_DESCRIPTOR	= "()Ljava/lang/Object;"; 
    	final String METHOD_NAME		= "topAndPop";
    	final String OUTPUT_FILE_PATH	= "tmp/JBSETest-StackLi.txt";
    	final String SETTINGS_FILE		= "HEXsettings/kiasan.jbse";
    	
		makeTest(METHOD_CLASS, METHOD_DESCRIPTOR, METHOD_NAME,
				OUTPUT_FILE_PATH, SETTINGS_FILE, null);
	}
	
}
