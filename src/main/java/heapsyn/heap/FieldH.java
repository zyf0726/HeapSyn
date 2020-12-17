package heapsyn.heap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldH implements Comparable<FieldH> {
	
	private static Map<Field, FieldH> INSTANCE = new HashMap<>();
	
	private Field javaField;

	private FieldH(Field javaField) {
		if (javaField == null)
			throw new IllegalArgumentException("a non-null class field expected");
		this.javaField = javaField;
	}
	
	public static FieldH I(Field javaField) {
		if (javaField == null)
			throw new IllegalArgumentException("a non-null class field expected");
		
		if (!INSTANCE.containsKey(javaField)) {
			INSTANCE.put(javaField, new FieldH(javaField));
		}
		return INSTANCE.get(javaField);
	}
	
	@Override
	public int compareTo(FieldH other) {
		return this.toString().compareTo(other.toString());
	}
	
	public String getName() {
		return this.javaField.getName();
	}
	
}
