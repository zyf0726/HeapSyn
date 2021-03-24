package heapsyn.wrapper.symbolic;

/**
 * @author Zhu Ruidong
 */

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.common.exceptions.UnhandledJBSEValue;
import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.smtlib.IntVar;
import jbse.mem.Heap;
import jbse.mem.HeapObjekt;
import jbse.mem.ObjektImpl;
import jbse.mem.PathCondition;
import jbse.mem.Variable;
import jbse.val.PrimitiveSymbolicLocalVariable;
import jbse.val.PrimitiveSymbolicMemberField;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

public class JBSEHeapTransformer {
	
	private static int MAX_HEAP_SIZE_JBSE = 1_000_000; 

	// keep only the useful HeapObjekts in Heap
	private static Heap filterPreObjekt(Heap heap) { 
		Heap ret = new Heap(MAX_HEAP_SIZE_JBSE);
		for (Entry<Long, HeapObjekt> entry : heap.__getObjects().entrySet()) {
			if (entry.getKey() >= heap.getStartPosition()) {
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
	
	public static Map<HeapObjekt, ObjectH> updateJBSEObjMap(
			Heap heap, PathCondition pathCond, Map<HeapObjekt, ObjectH> jbseObjMap) {
		Map<HeapObjekt, ObjectH> ret = new HashMap<>(jbseObjMap);
		Heap delHeap = filterPreObjekt(heap);
		Map<Long, HeapObjekt> objekts = delHeap.__getObjects();
		
		for (HeapObjekt o : objekts.values()) {
			if (!ret.containsKey(o)) {
				ret.put(o, transHeapObjektToObjectH((ObjektImpl) o));
			}
		}
		
		for (Entry<HeapObjekt, ObjectH> entry : ret.entrySet()) {
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
					ObjectH value = ret.get(objekt);
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
				 			value = ret.get(objekts.get(pos));
				 		}
				 	} else {
				 		value = ObjectH.NULL;
				 	}
				 	fieldValMap.put(field, value);
				} else if (varValue instanceof PrimitiveSymbolicLocalVariable || 
						varValue instanceof PrimitiveSymbolicMemberField) {
					ObjectH value = new ObjectH(new IntVar()); // TODO BoolVar?
					fieldValMap.put(field, value);
				} else {
					throw new UnhandledJBSEValue(varValue.getClass().getName()); 
				}
			}
			oh.setFieldValueMap(fieldValMap);
		}
		return ret;
	}

}
