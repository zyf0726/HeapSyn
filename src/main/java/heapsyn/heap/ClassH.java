package heapsyn.heap;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import heapsyn.smtlib.SMTSort;

public final class ClassH {
	
	private static Map<Class<?>, ClassH> INSTANCE_JAVA = new HashMap<>();
	private static Map<SMTSort, ClassH> INSTANCE_SMT = new HashMap<>();
	private static ClassH NULL_CLASS = new ClassH();
	
	private Class<?> javaClass;
	private SMTSort smtSort;
	
	private ClassH() {
		this.javaClass = null;
		this.smtSort = null;
	}
	
	private ClassH(Class<?> javaClass) {
		Preconditions.checkNotNull(javaClass, "a non-null java class expected");
		this.javaClass = javaClass;
		this.smtSort = null;
	}
	
	private ClassH(SMTSort smtSort) {
		Preconditions.checkNotNull(smtSort, "a non-null SMT-LIB sort expected");
		this.javaClass = null;
		this.smtSort = smtSort;
	}
	
	public static ClassH of() {
		return NULL_CLASS;
	}
	
	public static ClassH of(Class<?> javaClass) {
		if (!INSTANCE_JAVA.containsKey(javaClass)) {
			INSTANCE_JAVA.put(javaClass, new ClassH(javaClass));
		}
		return INSTANCE_JAVA.get(javaClass);
	}
	
	public static ClassH of(SMTSort smtSort) {
		if (!INSTANCE_SMT.containsKey(smtSort)) {
			INSTANCE_SMT.put(smtSort, new ClassH(smtSort));
		}
		return INSTANCE_SMT.get(smtSort);
	}
	
	public Class<?> getJavaClass() {
		return this.javaClass;
	}
	
	public SMTSort getSMTSort() {
		return this.smtSort;
	}
	
	public boolean isNullClass() {
		return (this.javaClass == null) && (this.smtSort == null);
	}
	
	public boolean isNonNullClass() {
		return this.javaClass != null;
	}
	
	public boolean isJavaClass() {
		return this.smtSort == null;
	}
	
	public boolean isSMTSort() {
		return this.smtSort != null;
	}
	
}
