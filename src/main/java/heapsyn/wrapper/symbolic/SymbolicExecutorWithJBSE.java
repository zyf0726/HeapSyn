package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import heapsyn.algo.MethodInvoke;
import heapsyn.common.exceptions.UnhandledJBSEPrimitive;
import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.common.exceptions.UnsupportedPrimitiveType;
import heapsyn.common.exceptions.UnsupportedSMTOperator;
import heapsyn.common.settings.JBSEParameters;
import heapsyn.common.settings.Options;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeap;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.*;
import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.Heap;
import jbse.mem.HeapObjekt;
import jbse.mem.PathCondition;
import jbse.mem.State;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;

public class SymbolicExecutorWithJBSE implements SymbolicExecutor {

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
	
	private Map<ObjectH, ObjectH> rvsobjSrcMap;
	
	static public int __countExecution=0;

	private static RunParameters getRunParameters(MethodInvoke mInvoke) {
		JBSEParameters parms = new JBSEParameters();
		Options options = Options.I();
		parms.setOutFilePath(null);
		parms.setShowOnConsole(false);
		parms.setTargetClassPath(options.getTargetClassPath());
		parms.setTargetSourcePath(options.getTargetSourcePath());
		parms.setTargetMethod(mInvoke.getJavaMethod());
		return parms.getRunParameters();
	}
	
	private Map<Primitive,ObjectH> obj2ref(Map<HeapObjekt,ObjectH> m) {
		Map<Primitive,ObjectH> ret=new HashMap<>();
		for(Entry<HeapObjekt,ObjectH> entry:m.entrySet()) {
			HeapObjekt ho=entry.getKey();
			ObjectH oh=entry.getValue();
			Primitive prim=ho.getIdentityHashCode();
			//Integer Int=(Integer) s.getActualValue();
			ret.put(prim, oh);
		}
		return ret;
	}
	
	/* get objSrcMap from maps between HeapObjekt and ObjectH */
	private Map<ObjectH, ObjectH> getobjSrcMap(Map<HeapObjekt, ObjectH> init,
			Map<HeapObjekt, ObjectH> fin) {
		Map<ObjectH, ObjectH> ret=new HashMap<>();
		Map<Primitive,ObjectH> rinit=this.obj2ref(init);
		Map<Primitive,ObjectH> rfin=this.obj2ref(fin);
		
		this.rvsobjSrcMap=new HashMap<>();
		
		for(Entry<Primitive,ObjectH> entry:rinit.entrySet()) {
			Primitive ho=entry.getKey();
			ObjectH oh=entry.getValue();
			if(rfin.containsKey(ho)) {
				ret.put(rfin.get(ho),oh);
				this.rvsobjSrcMap.put(oh, rfin.get(ho));
			}
		}
		return ret;
	}
	
	private Map<Primitive,ObjectH> getinitJBSEVarMap(State state,MethodInvoke mInvoke,Map<Primitive,ObjectH> preinit) {
		Map<Primitive,ObjectH> ret=new HashMap<>(preinit);
		Value[] vargs=state.getVargs();
		ArrayList<ObjectH> margs=mInvoke.getInvokeArguments();
		for(int i=0;i<vargs.length;++i) {
			if(vargs[i] instanceof Primitive) {
				ret.put((Primitive) vargs[i], margs.get(i));
			}
		}
		return ret;
	}
	
	private Set<ObjectH> getaccObjs(Set<ObjectH> init) {
		Set<ObjectH> ret=new HashSet<>();
		for(ObjectH oh:init) {
			if(this.rvsobjSrcMap.containsKey(oh)) {
				ret.add(this.rvsobjSrcMap.get(oh));
			}
		}
		return ret;
	}
	
	private SMTExpression JBSEexpr2SMTexpr(Map<Primitive,ObjectH> init,Primitive p) {
		if(init.containsKey(p)) return init.get(p).getVariable();
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
				return new ApplyExpr(smtop,JBSEexpr2SMTexpr(init,snd));
			}
			else {
				return new ApplyExpr(smtop,JBSEexpr2SMTexpr(init,fst),JBSEexpr2SMTexpr(init,snd));
			}
		} else {
			throw new UnhandledJBSEPrimitive(p.getClass().getName());
		}
	}
	
	/* get varExprMap from maps between Primitive and ObjectH */
	private Map<heapsyn.smtlib.Variable,SMTExpression> getvarExprMap(Map<Primitive,ObjectH> init,
			Map<ObjectH,Primitive> fin) {
		Map<heapsyn.smtlib.Variable,SMTExpression> ret=new HashMap<>();
		for(Entry<ObjectH,Primitive> entry:fin.entrySet()) {
			ObjectH oh=entry.getKey();
			Primitive p=entry.getValue();
			ret.put(oh.getVariable(), this.JBSEexpr2SMTexpr(init, p));
		}
		return ret;
	}
	
	private SMTExpression getPathcond(State state,Map<Primitive,ObjectH> initjbseVarMap) {
		ArrayList<SMTExpression> pds=new ArrayList<>();
		PathCondition jbsepd=state.__getPathCondition();
		for(int i=state.argpos+state.refarglen;i<jbsepd.__getClauses().size();++i) {
			ClauseAssume ca =(ClauseAssume) jbsepd.__getClauses().get(i);
			pds.add(this.JBSEexpr2SMTexpr(initjbseVarMap,ca.getCondition()));
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
		if (!(initHeap instanceof SymbolicHeapWithJBSE))
			return null;
		
		SymbolicExecutorWithJBSE.__countExecution++;
		List<PathDescriptor> pds = new ArrayList<>();
		if(!Modifier.isStatic(mInvoke.getJavaMethod().getModifiers())&&mInvoke.getInvokeArguments().get(0).isNullObject()) return pds;
		SymbolicHeapWithJBSE symHeapJBSE = (SymbolicHeapWithJBSE) initHeap;
		//State initState=symHeapJBSE.getJBSEState();
//		Heap heap=null;
//		PathCondition pathCond=null;
		
//		if(initState!=null) {
//			heap = initState.__getHeap();
//			pathCond = initState.__getPathCondition(); // previous arg pathcond need to be deleted?
//			int ofset=initState.argpos;
//			for(int i=0;i<initState.refarglen;++i) {
//				pathCond.__getClauses().remove(i+ofset);
//			}
//		}
		Map<HeapObjekt, ObjectH> jbseObjMap = symHeapJBSE.getJBSEObjMap();
		RunParameters p = getRunParameters(mInvoke);
		ArrayList<ObjectH> invokeArgs = mInvoke.getInvokeArguments();
		if (invokeArgs.size() != 0 ) {
			HeapObjekt[] args = new HeapObjekt[invokeArgs.size()];
			for (int i = 0; i < invokeArgs.size(); ++i) {
				if(invokeArgs.get(i).isHeapObject()) {
					for (Entry<HeapObjekt, ObjectH> entry : jbseObjMap.entrySet()) {
						if (entry.getValue().equals(invokeArgs.get(i))) {
							args[i] = entry.getKey();
							break;
						}
					}
				}
				else args[i]=null;
			}
			p.setArguments(args);
		}
//		p.setInitHeap(heap);
//		p.setInitPathCond(pathCond);
//		p.setInitState(initState);
		p.setObjects(symHeapJBSE.getObjects());
		p.setClauses(symHeapJBSE.getClauses());
		p.setPrimid(symHeapJBSE.getPrimid());
		p.setRefid(symHeapJBSE.getRefid());
		Run r = new Run(p);
		r.run();

		HashSet<State> executed = r.getExecuted();
		for (State state:executed) {
			Set<ObjectH> initaccObjs = new HashSet<>(symHeapJBSE.getAccessibleObjects());
			Heap finHeapJBSE = state.__getHeap();
//			PathCondition pathCondJBSE = state.__getPathCondition();
			JBSEHeapTransformer jhs=new JBSEHeapTransformer();
			jhs.transform(state);
			Map<HeapObjekt, ObjectH> finjbseObjMap = jhs.getfinjbseObjMap();
			
			Map<HeapObjekt,ObjectH> initjbseObjMap=symHeapJBSE.getJBSEObjMap();
			Map<ObjectH, ObjectH> objSrcMap=this.getobjSrcMap(initjbseObjMap,finjbseObjMap);
			
			PathDescriptor pd = new PathDescriptor();
			
			Set<ObjectH> accObjs=getaccObjs(initaccObjs);
			accObjs.add(ObjectH.NULL);
			
			Map<Primitive,ObjectH> initjbseVarMap=this.getinitJBSEVarMap(state,mInvoke,symHeapJBSE.getJBSEVarMap());
			Map<Primitive,ObjectH> finjbseVarMap=jhs.getfinjbseVarMap();
			Map<ObjectH,Primitive> finVarjbseMap=jhs.getfinVarjbseMap();
			
			pd.varExprMap=this.getvarExprMap(initjbseVarMap, finVarjbseMap);
			
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
			else if(retVal instanceof Primitive) {
				pd.retVal=finjbseVarMap.get(retVal);
			}
			//else pd.retVal=null;
			
			ArrayList<Clause> clauses=new ArrayList<>();
			PathCondition jbsepd=state.__getPathCondition();
			for(int i=state.pdpos;i<jbsepd.__getClauses().size();++i) {
				clauses.add(jbsepd.__getClauses().get(i));
			}
			
			
			SymbolicHeap symHeap = new SymbolicHeapWithJBSE(accObjs, ExistExpr.ALWAYS_FALSE, jhs.getobjects(),clauses,state.getsf().getnextPrim(),state.getsf().getnextRef(), finjbseObjMap,finjbseVarMap);
			pd.finHeap = symHeap;
			pd.objSrcMap=objSrcMap;
			pd.pathCond=this.getPathcond(state, initjbseVarMap);
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
