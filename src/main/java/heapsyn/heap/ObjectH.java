package heapsyn.heap;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

import heapsyn.smtlib.Variable;

public class ObjectH implements Serializable {
	
	private static final long serialVersionUID = 3745421834161560074L;
	

	public static boolean STRICT_MODE = true;
	
	public static ObjectH NULL = new ObjectH();
	
	private ClassH clsH;
	private Variable var;
	private ImmutableSortedMap<FieldH, ObjectH> field2val;
	
	private Object readResolve() throws ObjectStreamException {
		if (this.var != null) {
			return new ObjectH(this.var);
		} else if (this.clsH != ClassH.CLS_NULL) {
			return new ObjectH(this.clsH, this.field2val);
		} else {
			return NULL;
		}
	}
	
	private ObjectH() {
		this.clsH = ClassH.CLS_NULL; 
		this.var = null;
		this.field2val = ImmutableSortedMap.of();
	}
	
	public ObjectH(Variable var) {
		Preconditions.checkNotNull(var, "a non-null variable expected");
		this.clsH = ClassH.of(var.getSMTSort());
		this.var = var;
		this.field2val = ImmutableSortedMap.of();
	}
	
	public ObjectH(ClassH classH, Map<FieldH, ObjectH> fieldValueMap) {
		Preconditions.checkNotNull(classH, "a non-null classH expected");
		Preconditions.checkArgument(classH != ClassH.CLS_NULL, "null object already created");
		this.clsH = classH;
		this.var = null;
		if (fieldValueMap != null) {
			this.setFieldValueMap(fieldValueMap);
		} else {
			this.field2val = null;  // to be determined by invoking setFieldValueMap later
		}
	}
	
	public boolean isNullObject() {
		return this.clsH.isNullClass();
	}
	public boolean isNonNullObject() {
		return this.clsH.isNonNullClass();
	}
	public boolean isHeapObject() {
		return this.clsH.isJavaClass();
	}
	public boolean isVariable() {
		return this.clsH.isSMTSort();
	}
	
	public ClassH getClassH() {
		return this.clsH;
	}
	public Variable getVariable() {
		return this.var;
	}
	
	public Set<FieldH> getFields() {
		return this.field2val.keySet();
	}
	public Collection<ObjectH> getValues() {
		return this.field2val.values();
	}
	public Set<Entry<FieldH, ObjectH>> getEntries() {
		return this.field2val.entrySet();
	}
	public ObjectH getFieldValue(FieldH field) {
		return this.field2val.get(field);
	}
	
	public void setFieldValueMap(Map<FieldH, ObjectH> fieldValueMap) {
		Preconditions.checkState(!STRICT_MODE || this.field2val == null, "field-value map already determined");
		this.field2val = ImmutableSortedMap.copyOf(fieldValueMap);
	}
	
	@Deprecated 
	public void setFieldValue(FieldH field,ObjectH obj) {
		Map<FieldH, ObjectH> fieldvalMap=new HashMap<>(this.field2val);
		fieldvalMap.put(field, obj);
		this.field2val=ImmutableSortedMap.copyOf(fieldvalMap);
	}
	 
}
