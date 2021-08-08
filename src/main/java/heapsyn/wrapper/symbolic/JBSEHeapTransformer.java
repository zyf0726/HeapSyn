package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.TreeMap;

import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.common.exceptions.UnhandledJBSEValue;
import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.IntVar;
import jbse.mem.HeapObjekt;
import jbse.mem.Objekt;
import jbse.mem.ObjektImpl;
import jbse.mem.PathCondition;
import jbse.mem.ReachableObjectsCollector;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.FrozenStateException;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

public class JBSEHeapTransformer {
	
	public static ObjectH BLANK_OBJ = new ObjectH(ClassH.of(JBSEHeapTransformer.class), null);
	
	// private static int MAX_HEAP_SIZE_JBSE = 1_000_000; 
	private Map<HeapObjekt, ObjectH> finjbseObjMap = new HashMap<>();
	private Map<Primitive, ObjectH> finjbseVarMap = new HashMap<>();
	private Map<ObjectH,Primitive> finVarjbseMap = new HashMap<>(); // a Primitive may correspond to more than one ObjectH
	private Map<ObjectH,ReferenceSymbolic> ObjRefSym = new HashMap<>();
	private Map<ObjectH,ReferenceConcrete> ObjRefCon=new HashMap<>();
	private Map<ReferenceSymbolic,ObjectH> RefObjSym = new HashMap<>();
	private Map<ReferenceConcrete,ObjectH> RefObjCon=new HashMap<>();
	private Map<Long,HeapObjekt> objects;
	private Predicate<String> fieldFilter;
	
	public JBSEHeapTransformer(Predicate<String> fieldFilter) {
		this.fieldFilter=fieldFilter;
	}
	
	public Map<HeapObjekt, ObjectH> getfinjbseObjMap() {
		return this.finjbseObjMap;
	}
	
	public Map<Primitive, ObjectH> getfinjbseVarMap() {
		return this.finjbseVarMap;
	}
	
	public Map<ObjectH,Primitive> getfinVarjbseMap() {
		return this.finVarjbseMap;
	}
	
	public Map<ReferenceSymbolic,ObjectH> getRefObjSym() {
		return this.RefObjSym;
	}
	
	public Map<ReferenceConcrete,ObjectH> getRefObjCon() {
		return this.RefObjCon;
	}
	
	public Map<ObjectH,ReferenceSymbolic> getObjRefSym() {
		return this.ObjRefSym;
	}
	
	public Map<ObjectH,ReferenceConcrete> getObjRefCon() {
		return this.ObjRefCon;
	}
	
	public Map<Long,HeapObjekt> getobjects() {
		return this.objects;
	}

	// keep only the useful HeapObjekts in Heap
//	private static Heap filterPreObjekt(Heap heap) { 
//		Heap ret = new Heap(MAX_HEAP_SIZE_JBSE);
//		for (Entry<Long, HeapObjekt> entry : heap.__getObjects().entrySet()) {
//			if (entry.getKey() >= heap.getStartPosition()) {
//				HeapObjekt o=entry.getValue();
//				if(o instanceof InstanceWrapper_DEFAULT) {
//					((InstanceWrapper_DEFAULT) o).possiblyCloneDelegate();
//				}
//				ret.__getObjects().put(entry.getKey(), entry.getValue());
//			}
//		}
//		return ret;
//	}
	
	// transform a HeapObjekt to an ObjectH (with fieldValueMap undetermined)
	private static ObjectH transHeapObjektToObjectH(ObjektImpl o) {
		try {
			String clsName = o.getType().getClassName().replace('/','.');
			Class<?> javaClass = Class.forName(clsName);
			return new ObjectH(ClassH.of(javaClass), null);
		} catch (ClassNotFoundException e) {
			// this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
    //copied from java.util.stream.Collectors
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
	
	public boolean transform(State state)  {		
		//Heap heap=state.__getHeap();
		PathCondition pathCond=state.__getPathCondition();
		
		//Heap delHeap = filterPreObjekt(heap);
		Set<Map.Entry<Long, Objekt>> entries = null;
		try {
			final Set<Long> reachable= new ReachableObjectsCollector().reachable(state, false);
			entries = state.getHeap().entrySet().stream()
			        .filter(e -> reachable.contains(e.getKey()))
			        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), throwingMerger(), TreeMap::new)).entrySet();
		} catch (FrozenStateException e1) {
			throw new UnexpectedInternalException(e1);
		}

		//Map<Long, HeapObjekt> objekts = delHeap.__getObjects();
		Map<Long,HeapObjekt> objekts=new HashMap<>();
		for(Entry<Long,Objekt> entry: entries) {
			objekts.put(entry.getKey(), (HeapObjekt) entry.getValue());
		}
		this.objects=new HashMap<>(objekts);
		
		//Map<HeapObjekt, ObjectH> finjbseObjMap = new HashMap<>();
		
		for (HeapObjekt o : objekts.values()) {
			if(o.getOrigin()!=null&&o.getType().getClassName().replace('/', '.').equals(Object.class.getName())) {
				continue;
			}
			this.finjbseObjMap.put(o, transHeapObjektToObjectH((ObjektImpl) o));
		}
		
		for (Entry<HeapObjekt, ObjectH> entry : this.finjbseObjMap.entrySet()) {
			// determine fieldValMap for each ObjectH
			HeapObjekt ok = entry.getKey();
			ObjectH oh = entry.getValue();
			Map<FieldH, ObjectH> fieldValMap = new HashMap<>();
			for (Variable var : ok.fields().values()) {
				if(this.fieldFilter.test(var.getName())==false) continue;
				FieldH field = null;
				String clsName="";
				try {
					clsName = ok.getType().getClassName().replace('/', '.');
					Class<?> javaClass = Class.forName(clsName);
					Field javaField = javaClass.getDeclaredField(var.getName());
					field = FieldH.of(javaField);
				} catch (NoSuchFieldException | SecurityException | ClassNotFoundException e) {
					// this should never happen
					return false;
					//throw new UnexpectedInternalException(e);
				}
				Value varValue = var.getValue();
				if (varValue instanceof ReferenceConcrete) {
					ReferenceConcrete rc = (ReferenceConcrete) varValue;
					HeapObjekt objekt = objekts.get(rc.getHeapPosition());
					if(objekt!=null&&objekt.getType().getClassName().replace('/', '.').equals(Object.class.getName())) {
						ObjectH value=new ObjectH(ClassH.of(Object.class),new HashMap<FieldH, ObjectH>());
						fieldValMap.put(field, value);
						this.ObjRefCon.put(value,rc);
						this.RefObjCon.put(rc, value);
					}
					else {
						ObjectH value = this.finjbseObjMap.get(objekt);
						if (value == null) {
							fieldValMap.put(field, ObjectH.NULL);
						} else {
							fieldValMap.put(field, value);
						}
					}
				} else if (varValue instanceof ReferenceSymbolic) {
					ReferenceSymbolic ref = (ReferenceSymbolic) varValue;
					ObjectH value = null;
				 	if (pathCond.resolved(ref)) {
				 		if(ref.getStaticType().equals("Ljava/lang/Object;")) {
				 			//System.out.println(ref.getStaticType());
				 			value=new ObjectH(ClassH.of(Object.class),new HashMap<FieldH, ObjectH>());
				 			this.ObjRefSym.put(value, ref);
				 			this.RefObjSym.put(ref, value);
				 		}
				 		else {
					 		Long pos = pathCond.getResolution(ref);
					 		
					 		if (pos == jbse.mem.Util.POS_NULL) {
					 			value = ObjectH.NULL;
					 		} else {
					 			HeapObjekt objekt=objekts.get(pos);
					 			value = this.finjbseObjMap.get(objekt);
					 		}
				 		}
				 	} else {
				 		value = BLANK_OBJ;
				 	}
				 	fieldValMap.put(field, value);
				} else if (varValue instanceof Primitive) {
					ObjectH value=null; 
					if(varValue.getType()=='I') value = new ObjectH(new IntVar());
					else if(varValue.getType()=='Z') value=new ObjectH(new BoolVar());
					fieldValMap.put(field, value);
					this.finjbseVarMap.put((Primitive) varValue, value);
					this.finVarjbseMap.put(value,(Primitive) varValue);
				} 
				else {
					throw new UnhandledJBSEValue(varValue.getClass().getName()); 
				}
			}
			oh.setFieldValueMap(fieldValMap);
		}
		return true;
						
	}
}
	