package heapsyn.heap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import heapsyn.smtlib.Variable;

public class ObjectH {
	
	public static ObjectH NULL_OBJECT = new ObjectH();
	
	private ClassH clsH;
	private Variable var;
	private Map<FieldH, ObjectH> field2val;
	
	private ObjectH() {
		this.clsH = ClassH.I(); 
		this.var = null;
		this.setFieldValueMap(null);
	}
	
	public ObjectH(Variable var) {
		if (var == null)
			throw new IllegalArgumentException("a non-null variable expected");
		this.clsH = ClassH.I(var.getSMTSort());
		this.var = var;
		this.setFieldValueMap(null);
	}
	
	public ObjectH(ClassH classH, Map<FieldH, ObjectH> field2val) {
		if (classH == null)
			throw new IllegalArgumentException("a non-null classH expected");
		this.clsH = classH;
		this.var = null;
		this.setFieldValueMap(field2val);
	}
	
	public void setFieldValueMap(Map<FieldH, ObjectH> field2val) {
		if (field2val == null) {
			this.field2val = Collections.emptyMap();
		} else {
			this.field2val = new HashMap<>(field2val);
		}
	}
	
	public ClassH getClassH() {
		return this.clsH;
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
	
	public Set<FieldH> getFields() {
		return this.field2val.keySet();
	}
	public Collection<ObjectH> getValues() {
		return this.field2val.values();
	}
	public Set<Entry<FieldH, ObjectH>> getEntries() {
		return this.field2val.entrySet();
	}
	
	public ObjectH getValue(FieldH field) {
		return this.field2val.get(field);
	}	
	public Variable getVariable() {
		return this.var;
	}
	 
}
