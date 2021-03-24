package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import heapsyn.algo.MethodInvoke;
import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.mem.Heap;
import jbse.mem.HeapObjekt;
import jbse.mem.PathCondition;
import jbse.val.ReferenceConcrete;
import jbse.val.Value;

public class SymbolicExecutorWithJBSE implements SymbolicExecutor {
	
	// Customize them
    private static final String TARGET_CLASSPATH  = "bin/test/";
    private static final String TARGET_SOURCEPATH = "src/test/java/";
    
    // Leave them alone
    private static final String Z3_PATH			= "libs/z3.exe";
    private static final String JBSE_HOME		= "jbse/";
    private static final String JBSE_CLASSPATH	= JBSE_HOME + "build/classes/java/main";
    private static final String JBSE_SOURCEPATH	= JBSE_HOME + "src/main/java/";
    private static final String JRE_SOURCEPATH	= System.getProperty("java.home", "") + "src.zip";
    private static final String[] CLASSPATH		= { TARGET_CLASSPATH };
    private static final String[] SOURCEPATH	= { JBSE_SOURCEPATH, TARGET_SOURCEPATH, JRE_SOURCEPATH };
    
	
	// https://stackoverflow.com/questions/45072268/how-can-i-get-the-signature-field-of-java-reflection-method-object
	private static String getSignature(Method m) {
		String sig;
		try {
			Field gSig = Method.class.getDeclaredField("signature");
			gSig.setAccessible(true);
			sig = (String) gSig.get(m);
			if (sig != null) return sig;
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
	
	private static void set(RunParameters p, MethodInvoke mInvoke) {
		Method method = mInvoke.getJavaMethod();
		p.setJBSELibPath(JBSE_CLASSPATH);
		p.addUserClasspath(CLASSPATH);
		p.addSourcePath(SOURCEPATH);
		p.setMethodSignature(method.getDeclaringClass().getName().replace('.', '/'),
				getSignature(method), method.getName());
		p.setDecisionProcedureType(DecisionProcedureType.Z3);
		p.setExternalDecisionProcedurePath(Z3_PATH);
		p.setDoSignAnalysis(true);
		p.setDoEqualityAnalysis(true);
		p.setStateFormatMode(StateFormatMode.TEXT);
		p.setStepShowMode(StepShowMode.LEAVES);
		p.setOutputFileNone();
		p.setShowOnConsole(false);
	}

	@Override
	public Collection<PathDescriptor> executeMethod(SymbolicHeap initHeap, MethodInvoke mInvoke) {
		if (!(initHeap instanceof SymbolicHeapWithJBSE))
			return null;
		
		SymbolicHeapWithJBSE symHeapJBSE = (SymbolicHeapWithJBSE) initHeap;
		Heap heap = symHeapJBSE.getJBSEHeap();
		PathCondition pathCond = symHeapJBSE.getJBSEPathCond();
		Map<HeapObjekt, ObjectH> jbseObjMap = symHeapJBSE.getJBSEObjMap();
		RunParameters p = new RunParameters();
		ArrayList<ObjectH> invokeArgs = mInvoke.getInvokeArguments();
		if (invokeArgs != null) {
			HeapObjekt[] args = new HeapObjekt[invokeArgs.size()];
			for (int i = 0; i < invokeArgs.size(); ++i) {
				for (Entry<HeapObjekt, ObjectH> entry : jbseObjMap.entrySet()) {
					if (entry.getValue().equals(invokeArgs.get(i))) {
						args[i] = entry.getKey();
						break;
					}
				}
			}
			p.setArguments(args);
		}
		set(p, mInvoke);
		p.setInitHeap(heap);
		p.setInitPathCond(pathCond);
		Run r = new Run(p);
		r.run();
		
		Map<Heap, PathCondition> executed = r.getExecuted();
		List<PathDescriptor> pds = new ArrayList<>();
		for (Entry<Heap, PathCondition> entry : executed.entrySet()) {
			Set<ObjectH> accObjs = new HashSet<>(symHeapJBSE.getAccessibleObjects());
			Heap finHeapJBSE = entry.getKey();
			PathCondition pathCondJBSE = entry.getValue();
			Map<HeapObjekt, ObjectH> jbseObjMapUpd = JBSEHeapTransformer.updateJBSEObjMap(
					finHeapJBSE, pathCondJBSE, jbseObjMap);
			Value retVal = finHeapJBSE.getReturnValue();
			if (retVal instanceof ReferenceConcrete) {
				ReferenceConcrete refRetVal = (ReferenceConcrete) retVal;
				Long pos = refRetVal.getHeapPosition();
				HeapObjekt ho = finHeapJBSE.__getObjects().get(pos);
				if (ho != null) {
					accObjs.add(jbseObjMapUpd.get(ho));
				} else {
					// this should never happen
					throw new UnexpectedInternalException("returned object not in the final heap");
				}
			}
			SymbolicHeap symHeap = new SymbolicHeapWithJBSE(accObjs, null,
					finHeapJBSE, pathCond, jbseObjMapUpd);
			PathDescriptor pd = new PathDescriptor();
			pd.finHeap = symHeap;
			// TODO what about other fields?
			pds.add(pd);
		}
		return pds;
	}

	@Override
	public Collection<PathDescriptor> executeMethodUnderTest(MethodInvoke mInvoke) {
		// TODO Auto-generated method stub
		return null;
	}

}
