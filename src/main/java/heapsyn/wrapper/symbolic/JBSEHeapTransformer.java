package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.common.exceptions.UnhandledJBSEValue;
import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.smtlib.BoolVar;
import heapsyn.smtlib.IntVar;
import jbse.mem.Heap;
import jbse.mem.HeapObjekt;
import jbse.mem.InstanceWrapper_DEFAULT;
import jbse.mem.ObjektImpl;
import jbse.mem.PathCondition;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

public class JBSEHeapTransformer {
	
	private static int MAX_HEAP_SIZE_JBSE = 1_000_000; 
	private Map<HeapObjekt, ObjectH> finjbseObjMap = new HashMap<>();
	private Map<Primitive, ObjectH> finjbseVarMap = new HashMap<>();
	private Map<ObjectH,Primitive> finVarjbseMap = new HashMap<>(); // a Primitive may correspond to more than one ObjectH
	private TreeMap<Long,HeapObjekt> objects;
	
	public Map<HeapObjekt, ObjectH> getfinjbseObjMap() {
		return this.finjbseObjMap;
	}
	
	public Map<Primitive, ObjectH> getfinjbseVarMap() {
		return this.finjbseVarMap;
	}
	
	public Map<ObjectH,Primitive> getfinVarjbseMap() {
		return this.finVarjbseMap;
	}
	
	public TreeMap<Long,HeapObjekt> getobjects() {
		return this.objects;
	}

	// keep only the useful HeapObjekts in Heap
	private static Heap filterPreObjekt(Heap heap) { 
		Heap ret = new Heap(MAX_HEAP_SIZE_JBSE);
		for (Entry<Long, HeapObjekt> entry : heap.__getObjects().entrySet()) {
			if (entry.getKey() >= heap.getStartPosition()) {
				HeapObjekt o=entry.getValue();
				if(o instanceof InstanceWrapper_DEFAULT) {
					((InstanceWrapper_DEFAULT) o).possiblyCloneDelegate();
				}
				ret.__getObjects().put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
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
	
	public void transform(State state) {		
		Heap heap=state.__getHeap();
		PathCondition pathCond=state.__getPathCondition();
		
		Heap delHeap = filterPreObjekt(heap);
		Map<Long, HeapObjekt> objekts = delHeap.__getObjects();
		this.objects=new TreeMap<>(objekts);
		
		//Map<HeapObjekt, ObjectH> finjbseObjMap = new HashMap<>();
		
		for (HeapObjekt o : objekts.values()) {
//			if(o instanceof InstanceWrapper_DEFAULT) {
//				o=o.ge
//			}
			this.finjbseObjMap.put(o, transHeapObjektToObjectH((ObjektImpl) o));
		}
		
		for (Entry<HeapObjekt, ObjectH> entry : this.finjbseObjMap.entrySet()) {
			// determine fieldValMap for each ObjectH
			HeapObjekt ok = entry.getKey();
			ObjectH oh = entry.getValue();
			Map<FieldH, ObjectH> fieldValMap = new HashMap<>();
			for (Variable var : ok.fields().values()) {
				FieldH field = null;
				try {
					String clsName = ok.getType().getClassName().replace('/', '.');
					Class<?> javaClass = Class.forName(clsName);
					Field javaField = javaClass.getDeclaredField(var.getName());
					field = FieldH.of(javaField);
				} catch (NoSuchFieldException | SecurityException | ClassNotFoundException e) {
					// this should never happen
					throw new UnexpectedInternalException(e);
				}
				Value varValue = var.getValue();
				if (varValue instanceof ReferenceConcrete) {
					ReferenceConcrete rc = (ReferenceConcrete) varValue;
					HeapObjekt objekt = objekts.get(rc.getHeapPosition());
					ObjectH value = this.finjbseObjMap.get(objekt);
					if (value == null) {
						fieldValMap.put(field, ObjectH.NULL);
					} else {
						fieldValMap.put(field, value);
					}
				} else if (varValue instanceof ReferenceSymbolic) {
					ReferenceSymbolic ref = (ReferenceSymbolic) varValue;
					ObjectH value = null;
				 	if (pathCond.resolved(ref)) {
				 		Long pos = pathCond.getResolution(ref);
				 		if (pos == jbse.mem.Util.POS_NULL) {
				 			value = ObjectH.NULL;
				 		} else {
				 			value = this.finjbseObjMap.get(objekts.get(pos));
				 		}
				 	} else {
				 		value = ObjectH.NULL;
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
						
	}
}
	