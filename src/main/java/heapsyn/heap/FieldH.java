package heapsyn.heap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public final class FieldH implements Comparable<FieldH> {
	
	private static Map<Field, FieldH> INSTANCE = new HashMap<>();
	
	private Field javaField;

	private FieldH(Field javaField) {
		Preconditions.checkNotNull(javaField, "a non-null java class field expected");
		this.javaField = javaField;
	}
	
	public static FieldH of(Field javaField) {
		if (!INSTANCE.containsKey(javaField)) {
			INSTANCE.put(javaField, new FieldH(javaField));
		}
		return INSTANCE.get(javaField);
	}
	
	@Override
	public int compareTo(FieldH other) {
		return this.javaField.toString().compareTo(other.javaField.toString());
	}
	
	public String getName() {
		return this.javaField.getName();
	}
	
	public Field getJavaField() {
		return this.javaField;
	}
}
