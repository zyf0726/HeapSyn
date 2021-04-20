package heapsyn.wrapper.symbolic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import heapsyn.algo.WrappedHeap;
import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;
import heapsyn.heap.ObjectH;
import heapsyn.heap.SymbolicHeapAsDigraph;
import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.SMTSort;
import heapsyn.smtlib.Variable;

public class SpecFactory {
	
	// symbol table
	private HashMap<String, ObjectH> refSymTab;
	private HashMap<String, ObjectH> varSymTab;
	
	// specification
	private Set<ObjectH> accRefs;
	private List<SMTExpression> varConds;
	
	private ObjectH getRefOrDecl(Class<?> javaClass, String refName) {
		ObjectH o = this.refSymTab.get(refName);
		if (o == null) {
			o = new ObjectH(ClassH.of(javaClass), null);
			this.refSymTab.put(refName, o);
		}
		return o;
	}
	
	private ObjectH getVarOrDecl(SMTSort smtSort, String varName) {
		ObjectH o = this.varSymTab.get(varName);
		if (o == null) {
			o = new ObjectH(Variable.create(smtSort));
			this.varSymTab.put(varName, o);
		}
		return o;
	}
	
	public ObjectH mkRefDecl(Class<?> javaClass, String refName) {
		Preconditions.checkArgument(!this.refSymTab.containsKey(refName),
				"duplicated reference symbol %s", refName);
		ObjectH o = new ObjectH(ClassH.of(javaClass), null);
		this.refSymTab.put(refName, o);
		return o;
	}
	
	public ObjectH mkVarDecl(SMTSort smtSort, String varName) {
		Preconditions.checkArgument(!this.varSymTab.containsKey(varName),
				"duplicated variable symbol %s", varName);
		ObjectH o = new ObjectH(Variable.create(smtSort));
		this.varSymTab.put(varName, o);
		return o;
	}
	
	public boolean addRefSpec(String refName, String... specs) {
		ObjectH o = this.refSymTab.get(refName);
		if (o == null) return false;
		
		Class<?> javaClass = o.getClassH().getJavaClass();
		Map<FieldH, ObjectH> fieldValMap = new HashMap<>();
		for (int i = 1; i < specs.length; i += 2) {
			String fieldName = specs[i - 1];
			Field javaField = null;
			try {
				javaField = javaClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				return false;
			}
			Class<?> fieldType = javaField.getType();
			String valueName = specs[i];
			ObjectH value = null;
			if (fieldType.isPrimitive()) {
				SMTSort sort = SpecFactory.toSMTSort(fieldType);
				if (sort != null) {
					value = this.getVarOrDecl(sort, valueName);
				} else {
					return false;
				}
			} else {
				value = this.getRefOrDecl(fieldType, valueName);
			}
			fieldValMap.put(FieldH.of(javaField), value);
		}
		o.setFieldValueMap(fieldValMap);
		return true;
	}
	
	public boolean addVarSpec(String e) {
		SMTExpression smtExpr = this.parseSMTExpr(e.trim());
		if (smtExpr == null) return false;
		this.varConds.add(smtExpr);
		return true;
	}
	
	public boolean setAccessible(String... refNames) {
		for (int i = 0; i < refNames.length; ++i) {
			if (!this.refSymTab.containsKey(refNames[i]))
				return false;
		}
		for (int i = 0; i < refNames.length; ++i) {
			this.accRefs.add(this.refSymTab.get(refNames[i]));
		}
		return true;
	}
	
	public Specification genSpec() {
		Specification spec = new Specification();
		spec.expcHeap = new SymbolicHeapAsDigraph(this.accRefs, null);
		if (this.varConds.isEmpty()) {
			spec.condition = new BoolConst(true);
		} else {
			spec.condition = new ApplyExpr(SMTOperator.AND, this.varConds);
		}
		return spec;
	}

	public SpecFactory() {
		this.refSymTab = new HashMap<>();
		this.varSymTab = new HashMap<>();
		this.accRefs = new HashSet<>();
		this.varConds = new ArrayList<>();
		this.refSymTab.put("null", ObjectH.NULL);
		this.accRefs.add(ObjectH.NULL);
	}

	private static SMTSort toSMTSort(Class<?> javaClass) {
		if (!javaClass.isPrimitive())
			return null;
		String typename = javaClass.getName();
		if (Arrays.asList("byte", "char", "int", "long", "short").contains(typename))
			return SMTSort.INT;
		if ("boolean".equals(typename))
			return SMTSort.BOOL;
		return null;
	}
	
	private SMTExpression parseSMTExpr(String e) {
		if (e.startsWith("(") && e.endsWith(")")) { // ApplyExpr
			String[] es = e.substring(1, e.length() - 1).trim().split(" ");
			SMTOperator op = null;
			for (SMTOperator smtop : SMTOperator.values()) {
				if (smtop.getName().equals(es[0])) {
					op = smtop;
					break;
				}
			}
			if (op == null) return null;
			int curDepth = 0;
			StringBuilder curExpr = new StringBuilder();
			List<SMTExpression> oprds = new ArrayList<>();
			for (int i = 1; i < es.length; ++i) {
				if (es[i].isEmpty()) continue;
				curDepth += es[i].chars().filter(ch -> ch == '(').count();
				curDepth -= es[i].chars().filter(ch -> ch == ')').count();
				curExpr.append(es[i]);
				if (curDepth == 0) {
					SMTExpression oprd = this.parseSMTExpr(curExpr.toString());
					if (oprd == null) return null;
					oprds.add(oprd);
					curExpr = new StringBuilder(); 
				} else {
					curExpr.append(' ');
				}
			}
			if (curDepth != 0) return null;
			return new ApplyExpr(op, oprds);
		} else if (e.equals("true")) { // BoolConst - true
			return new BoolConst(true);
		} else if (e.equals("false")) { // BoolConst - false
			return new BoolConst(false);
		} else {
			// IntConst
			Long v = null;
			try {
				v = Long.valueOf(e);
				return new IntConst(v);
			} catch (NumberFormatException exc) { }
			// Variable
			if (this.varSymTab.containsKey(e)) {
				return this.varSymTab.get(e).getVariable();
			} else {
				return null;
			}
		}
	}
	
	static class ListNode {
		static FieldH fNext, fValue;
		static {
			try {
				fNext = FieldH.of(ListNode.class.getDeclaredField("next"));
				fValue = FieldH.of(ListNode.class.getDeclaredField("value"));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		ListNode next;
		int value;
	}
	
	public static void main(String[] args) {
		SpecFactory specFty = new SpecFactory();
		ObjectH o1 = specFty.mkRefDecl(ListNode.class, "o1"); 
		Variable v1 = specFty.mkVarDecl(SMTSort.INT, "v1").getVariable();
		assert(specFty.addRefSpec("o1", "next", "o2", "value", "v1"));
		assert(specFty.addRefSpec("o2", "next", "o3", "value", "v2"));
		assert(specFty.addRefSpec("o3", "next", "o1", "value", "v3"));
		assert(specFty.addVarSpec("(= v3 (+ v1 v2))"));
		assert(specFty.addVarSpec("(distinct v1 0)"));
		assert(specFty.addVarSpec("(distinct v2 0)"));
		assert(specFty.setAccessible("o1"));
		assert(o1.getFieldValue(ListNode.fValue).getVariable() == v1);
		Specification spec = specFty.genSpec();
		System.out.println(spec.condition.toSMTString());
		new WrappedHeap(spec.expcHeap).__debugPrintOut(System.out);
	}
	
}
