package heapsyn.wrapper.symbolic;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import heapsyn.algo.MethodInvoke;
import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.common.exceptions.UnhandledJBSEPrimitive;
import heapsyn.common.exceptions.UnsupportedPrimitiveType;
import heapsyn.common.exceptions.UnsupportedSMTOperator;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.IntVar;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.ClauseAssumeReferenceSymbolic;
import jbse.mem.Heap;
import jbse.mem.HeapObjekt;
import jbse.mem.PathCondition;
import jbse.mem.State;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicLocalVariable;
import jbse.val.PrimitiveSymbolicMemberField;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicMemberField;
import jbse.val.Simplex;
import jbse.val.Value;

public class SymbolicExecutorWithCachedJBSE implements SymbolicExecutor{
	
	// Customize them
	private static final String TARGET_CLASSPATH = "bin/test/";
	private static final String TARGET_SOURCEPATH = "src/test/java/";

	// Leave them alone
	private static final String Z3_PATH = "libs/z3.exe";
	private static final String JBSE_HOME = "jbse/";
	private static final String JBSE_CLASSPATH = JBSE_HOME + "build/classes/java/main";
	private static final String JBSE_SOURCEPATH = JBSE_HOME + "src/main/java/";
	private static final String JRE_SOURCEPATH = System.getProperty("java.home", "") + "src.zip";
	private static final String[] CLASSPATH = { TARGET_CLASSPATH };
	private static final String[] SOURCEPATH = { JBSE_SOURCEPATH, TARGET_SOURCEPATH, JRE_SOURCEPATH };
	
	private static Map<Operator,SMTOperator> opMap=new HashMap<>();
	
	static {
		opMap.put(Operator.ADD, SMTOperator.ADD);
		opMap.put(Operator.SUB, SMTOperator.SUB);
		opMap.put(Operator.AND, SMTOperator.AND);
		opMap.put(Operator.OR, SMTOperator.OR);
		opMap.put(Operator.EQ, SMTOperator.BIN_EQ);
		opMap.put(Operator.NE, SMTOperator.BIN_NE);
		opMap.put(Operator.MUL, SMTOperator.MUL);
		opMap.put(Operator.NOT, SMTOperator.UN_NOT);
		
	}
		
	static public int __countExecution=0;

	// https://stackoverflow.com/questions/45072268/how-can-i-get-the-signature-field-of-java-reflection-method-object
	private static String getSignature(Method m) {
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

	private static void set(RunParameters p, MethodInvoke mInvoke) {
		Method method = mInvoke.getJavaMethod();
		p.setJBSELibPath(JBSE_CLASSPATH);
		p.addUserClasspath(CLASSPATH);
		p.addSourcePath(SOURCEPATH);
		p.setMethodSignature(method.getDeclaringClass().getName().replace('.', '/'), getSignature(method).replace('.', '/'),
				method.getName());
		p.setDecisionProcedureType(DecisionProcedureType.Z3);
		p.setExternalDecisionProcedurePath(Z3_PATH);
		p.setDoSignAnalysis(true);
		p.setDoEqualityAnalysis(true);
		p.setStateFormatMode(StateFormatMode.TEXT);
		p.setStepShowMode(StepShowMode.LEAVES);
		p.setOutputFileNone();
		p.setShowOnConsole(true);
	}
	
	
	private Map<Method,Set<State>> cachedJBSE;
	
	public SymbolicExecutorWithCachedJBSE() {
		this.cachedJBSE=new HashMap<>();
	}
	
	private Map<ReferenceSymbolic,ObjectH> ref2ObjMap(ArrayList<Clause> refclause,Map<Value,ObjectH> val2Obj) {
		Map<ReferenceSymbolic,ObjectH> ref2Obj=new HashMap<>();
		for(Entry<Value,ObjectH> entry:val2Obj.entrySet()) {
			if(entry.getKey() instanceof ReferenceSymbolic) {
				ref2Obj.put((ReferenceSymbolic)entry.getKey(), entry.getValue());
			}
		}
		
		for(int i=0;i<refclause.size();++i) {
			ClauseAssumeReferenceSymbolic clause=(ClauseAssumeReferenceSymbolic) refclause.get(i);
			ReferenceSymbolic ref=clause.getReference();
			Stack<ReferenceSymbolic> stack=new Stack<>();
			ReferenceSymbolic localref=ref;
			while(!ref2Obj.containsKey(localref)) {
				stack.push(localref);
				localref=((ReferenceSymbolicMemberField) localref).getContainer();
			}
			ObjectH obj=ref2Obj.get(localref);
			while(!stack.empty()) {
				if(obj.isNullObject()) return null;
				ReferenceSymbolicMemberField memberref=(ReferenceSymbolicMemberField) stack.pop();
				String fieldname=memberref.getFieldName();
				for(FieldH field:obj.getFields()) {
					if(field.getName().equals(fieldname)) {
						obj=obj.getFieldValue(field);
						ref2Obj.put(memberref, obj);
						break;
					}
				}
			}
		}
		
		return ref2Obj;
	}
	
	private boolean isSat(ArrayList<Clause> refclause,Map<ReferenceSymbolic,ObjectH> ref2Obj) {
		for(int i=0;i<refclause.size();++i) {
			ClauseAssumeReferenceSymbolic clause=(ClauseAssumeReferenceSymbolic) refclause.get(i);
			ReferenceSymbolic ref=clause.getReference();
			ObjectH obj=ref2Obj.get(ref);
			if(clause instanceof ClauseAssumeNull) {
				if(!obj.isNullObject()) return false;
			}
			else if(clause instanceof ClauseAssumeAliases){
				HeapObjekt heapObj=((ClauseAssumeAliases) clause).getObjekt();
				ReferenceSymbolic oriref=heapObj.getOrigin();
				if(obj!=ref2Obj.get(oriref)) return false;
			}
			else {
				if(obj.isNullObject()) return false;
				for(int j=0;j<i;++j) {
					ClauseAssumeReferenceSymbolic preclause=(ClauseAssumeReferenceSymbolic) refclause.get(j);
					if(obj==ref2Obj.get(preclause.getReference())) return false;
				}
			}
		}
		return true;
	}
	
	private Map<ReferenceSymbolic,ObjectH> obj2ref(Map<HeapObjekt,ObjectH> m) {
		Map<ReferenceSymbolic,ObjectH> ret=new HashMap<>();
		for(Entry<HeapObjekt,ObjectH> entry:m.entrySet()) {
			HeapObjekt ho=entry.getKey();
			ObjectH oh=entry.getValue();
			ret.put(ho.getOrigin(), oh);
		}
		return ret;
	}
	
	private Map<ObjectH, ObjectH> getchangedObjSrcMap(Map<ReferenceSymbolic, ObjectH> init,
			Map<HeapObjekt, ObjectH> fin) {
		Map<ObjectH, ObjectH> ret=new HashMap<>();
		Map<ReferenceSymbolic,ObjectH> rfin=this.obj2ref(fin);
		
		//this.rvsobjSrcMap=new HashMap<>();
		
		for(Entry<ReferenceSymbolic,ObjectH> entry:init.entrySet()) {
			ReferenceSymbolic ref=entry.getKey();
			ObjectH oh=entry.getValue();
			if(rfin.containsKey(ref)) {
				ret.put(rfin.get(ref),oh);
				//this.rvsobjSrcMap.put(oh, rfin.get(ho));
			}
		}
		return ret;
	}
	
	private SMTExpression JBSEexpr2SMTexpr(Map<ReferenceSymbolic,ObjectH> ref2Obj,Map<Value,ObjectH> val2Obj,Primitive p) {
		if(p instanceof PrimitiveSymbolicLocalVariable) {
			return val2Obj.get(p).getVariable();
		}
		if(p instanceof PrimitiveSymbolicMemberField) {
			PrimitiveSymbolicMemberField pfield=(PrimitiveSymbolicMemberField)p;
			ReferenceSymbolic ref=pfield.getContainer();
			ObjectH container=ref2Obj.get(ref);
			for(FieldH field:container.getFields()) {
				if(field.getName().equals(pfield.getFieldName())) {
					return container.getFieldValue(field).getVariable();
				}
			}
			return null;
		}
		else if(p instanceof Simplex) {
			Simplex s=(Simplex) p;
			if(s.getType()=='I') return new IntConst((Integer)s.getActualValue());
			else if(s.getType()=='Z') return new BoolConst((Boolean)s.getActualValue());
			else throw new UnsupportedPrimitiveType();
		}
		else if(p instanceof Expression) {
			Expression expr=(Expression) p;
			Primitive fst=expr.getFirstOperand();
			Primitive snd=expr.getSecondOperand();
			Operator op=expr.getOperator();
			SMTOperator smtop=opMap.get(op);
			if(smtop==null) throw new UnsupportedSMTOperator(op.toString());
			if(expr.isUnary()) {
				return new ApplyExpr(smtop,JBSEexpr2SMTexpr(ref2Obj,val2Obj,snd));
			}
			else {
				return new ApplyExpr(smtop,JBSEexpr2SMTexpr(ref2Obj,val2Obj,fst),JBSEexpr2SMTexpr(ref2Obj,val2Obj,snd));
			}
		} else {
			throw new UnhandledJBSEPrimitive(p.getClass().getName());
		}
	}
	
	private SMTExpression getPathcond(List<Clause> clauses,Map<ReferenceSymbolic,ObjectH> ref2Obj,Map<Value,ObjectH> val2Obj) {
		ArrayList<SMTExpression> pds=new ArrayList<>();
		for(int i=0;i<clauses.size();++i) {
			ClauseAssume ca =(ClauseAssume) clauses.get(i);
			pds.add(this.JBSEexpr2SMTexpr(ref2Obj,val2Obj,ca.getCondition()));
		}
		if(pds.size()==0) return null;
		else {
			ApplyExpr ret=(ApplyExpr)pds.get(0);
			for(int i=1;i<pds.size();++i) {
				ret=new ApplyExpr(SMTOperator.AND,ret,(ApplyExpr)pds.get(i));
			}
			return ret;
		}
	}

	@Override
	public Collection<PathDescriptor> executeMethod(SymbolicHeap initHeap, MethodInvoke mInvoke) {
		SymbolicExecutorWithCachedJBSE.__countExecution++;
		List<PathDescriptor> pds = new ArrayList<>();
		if(!Modifier.isStatic(mInvoke.getJavaMethod().getModifiers())&&mInvoke.getInvokeArguments().get(0).isNullObject()) return pds;
		
		Method method=mInvoke.getJavaMethod();
		if(!this.cachedJBSE.containsKey(method)) {
			RunParameters p = new RunParameters();
			set(p, mInvoke);
			Run r=new Run(p);
			r.run();
			HashSet<State> executed = r.getExecuted();
			this.cachedJBSE.put(method, executed);
		}
		
		Set<State> states=this.cachedJBSE.get(method);
		
		for(State state:states) {
			PathDescriptor pd=new PathDescriptor();
			
			Value[] vargs=state.getVargs();
			ArrayList<ObjectH> margs=mInvoke.getInvokeArguments();
			
			Map<Value,ObjectH> val2Obj=new HashMap<>();
			for(int i=0;i<margs.size();++i) {
				val2Obj.put(vargs[i], margs.get(i));
			}
			
			ArrayList<Clause> refclause=new ArrayList<>(); // clauses about reference
			ArrayList<Clause> primclause=new ArrayList<>(); // clauses about primitive
			
			PathCondition jbsepd=state.__getPathCondition();
			List<Clause> clauses=jbsepd.__getClauses();
			for(int i=state.pdpos;i<clauses.size();++i) {
				Clause clause=clauses.get(i);
				if(clause instanceof ClauseAssume) {
					primclause.add(clause);
				}
				else refclause.add(clause);
			}
			
			Map<ReferenceSymbolic,ObjectH> ref2Obj=this.ref2ObjMap(refclause, val2Obj); //map between Reference and ObjectH 
			
			if(!this.isSat(refclause, ref2Obj)) continue;
			
			JBSEHeapTransformer jhs=new JBSEHeapTransformer();
			jhs.transform(state);
			
			Map<HeapObjekt, ObjectH> finjbseObjMap = jhs.getfinjbseObjMap();
			
			Map<ObjectH, ObjectH> changedObjSrcMap=this.getchangedObjSrcMap(ref2Obj, finjbseObjMap);
			
			Map<ObjectH, ObjectH> rvschangedObjSrcMap=new HashMap<>();
			for(Entry<ObjectH, ObjectH> entry:changedObjSrcMap.entrySet()) {
				rvschangedObjSrcMap.put(entry.getValue(), entry.getKey());
			}
			
			Map<ObjectH, ObjectH> objSrcMap=new HashMap<>();
			Map<ObjectH, ObjectH> rvsobjSrcMap=new HashMap<>();
			Map<heapsyn.smtlib.Variable,SMTExpression> varExprMap=new HashMap<>();
			
			Set<ObjectH> allObjs=initHeap.getAllObjects();
			Collection<ObjectH> changedObjs=ref2Obj.values(); // ObjectHs in ref2Obj are changed(at least used) during the symbolic execution
			for(ObjectH obj:allObjs) { // find unchanged ObjectH
				if(!changedObjs.contains(obj)&&obj.isNonNullObject()) {
					ObjectH cpy=new ObjectH(obj.getClassH(),null);
					objSrcMap.put(cpy, obj);
					rvsobjSrcMap.put(obj, cpy);
				}
			}
			
			for(Entry<ObjectH,ObjectH> entry:objSrcMap.entrySet()) { //copy unchanged ObjectH
				ObjectH key=entry.getKey();
				ObjectH value=entry.getValue();
				Map<FieldH,ObjectH> field2val=new HashMap<>();
				for(FieldH field:value.getFields()) {
					FieldH finField=FieldH.of(field.getField());
					ObjectH val=value.getFieldValue(field);
					if(val.isVariable()) {
						ObjectH var=null;
						if(val.getVariable() instanceof IntVar) {
							var=new ObjectH(new IntVar());
						}
						else if(val.getVariable() instanceof BoolVar) {
							var=new ObjectH(new BoolVar());
						}
						field2val.put(finField, var);
						varExprMap.put(var.getVariable(), val.getVariable());
					}
					else {
						if(val.isNullObject()) {
							field2val.put(finField, ObjectH.NULL);
						}
						else if(rvsobjSrcMap.containsKey(val)) {
							field2val.put(finField, rvsobjSrcMap.get(val));
						}
						else {
							field2val.put(finField, rvschangedObjSrcMap.get(val));
						}
					}
				}
				key.setFieldValueMap(field2val);
			}
			
			for(ObjectH obj:finjbseObjMap.values()) { // fill fieldValueMap of changed ObjectH
				Set<FieldH> fields=obj.getFields();
				for(FieldH field:fields) {
					ObjectH val=obj.getFieldValue(field);
					if(val==ObjectH.BLANK) {
						ObjectH initObj=changedObjSrcMap.get(obj);
						if(initObj==null) {
						}
						for(FieldH initField:initObj.getFields()) {
							if(initField.getName().equals(field.getName())) {
								ObjectH initval=initObj.getFieldValue(initField);
								ObjectH newval=rvsobjSrcMap.get(initval);
								if(newval==null) obj.setFieldValue(field, ObjectH.NULL);
								else obj.setFieldValue(field, newval);
								break;
							}
						}
					}
				}
			}
			
			for(Entry<ObjectH,ObjectH> entry:changedObjSrcMap.entrySet()) { //reverse
				objSrcMap.put(entry.getKey(), entry.getValue());
			}
			for(Entry<ObjectH,ObjectH> entry:rvschangedObjSrcMap.entrySet()) { //reverse
				rvsobjSrcMap.put(entry.getKey(), entry.getValue());
			}
			
			Set<ObjectH> accObjs=new HashSet<>();
			accObjs.add(ObjectH.NULL);
			for(ObjectH obj:initHeap.getAccessibleObjects()) {
				if(obj.isNonNullObject()) accObjs.add(rvsobjSrcMap.get(obj));
			}
			
			//Map<Primitive,ObjectH> finjbseVarMap=jhs.getfinjbseVarMap();
			Map<ObjectH,Primitive> finVarjbseMap=jhs.getfinVarjbseMap();
			
			for(Entry<ObjectH,Primitive> entry:finVarjbseMap.entrySet()) {
				Primitive prim=entry.getValue();
				SMTExpression smt=this.JBSEexpr2SMTexpr(ref2Obj,val2Obj, prim);
				//if(smt==null) continue;
				varExprMap.put(entry.getKey().getVariable(), smt);
			}
			
			Heap finHeapJBSE=state.__getHeap();
			Value retVal = finHeapJBSE.getReturnValue();
			if(retVal==null||retVal.getType()=='0') pd.retVal=null;
			else if (retVal instanceof ReferenceConcrete) {
				ReferenceConcrete refRetVal = (ReferenceConcrete) retVal;
				Long pos = refRetVal.getHeapPosition();
				HeapObjekt ho = finHeapJBSE.__getObjects().get(pos);
				if (ho != null) {
					accObjs.add(finjbseObjMap.get(ho));
					pd.retVal=finjbseObjMap.get(ho);
				} else {
					// this should never happen
					throw new UnexpectedInternalException("returned object not in the final heap");
				}
			}
			else if(retVal instanceof ReferenceSymbolic) {
				pd.retVal=rvsobjSrcMap.get(ref2Obj.get(retVal));
			}
			else if(retVal instanceof Primitive) {
				pd.retVal=null;
			}
			
			SymbolicHeap symHeap = new SymbolicHeapAsDigraph(accObjs, ExistExpr.ALWAYS_FALSE);
			pd.finHeap = symHeap;
			pd.objSrcMap=objSrcMap;
			pd.varExprMap=varExprMap;
			pd.pathCond=this.getPathcond(primclause, ref2Obj,val2Obj);
			
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
