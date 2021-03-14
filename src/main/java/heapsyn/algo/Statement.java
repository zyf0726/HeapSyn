package heapsyn.algo;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import heapsyn.heap.ObjectH;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.Variable;

public class Statement {
	
	public Method javaMethod;
	public List<ObjectH> objArgs;
	public Map<Variable, Constant> constValues;
	public ObjectH returnValue;
	
	public Statement(List<ObjectH> objArgs, Map<Variable, Constant> constValues) {
		this.objArgs = ImmutableList.copyOf(objArgs);
		this.constValues = ImmutableMap.copyOf(constValues);
	}
	
	public Statement(MethodInvoke mInvoke, ObjectH retVal) {
		this.javaMethod = mInvoke.getJavaMethod();
		this.objArgs = mInvoke.getInvokeArguments();
		this.constValues = new HashMap<>();
		this.returnValue = retVal;
	}
	
	public static void printStatements(List<Statement> stmts, PrintStream ps) {
		Map<ObjectH, String> objNames = new HashMap<>();
		int countRetVals = 0;
		for (Statement stmt : stmts) {
			if (stmt.returnValue != null && stmt.returnValue.isNonNullObject()) {
				ObjectH o = stmt.returnValue;
				objNames.put(o, "o" + (countRetVals++));
				ps.print(o.getClassH().getJavaClass().getSimpleName() + " ");
				ps.print(objNames.get(o) + " = ");
			}
			if (stmt.javaMethod != null) {
				ps.print(stmt.javaMethod.getName() + "(");
			} else {
				ps.print("@Test(");
			}
			StringBuilder sb = new StringBuilder();
			for (ObjectH arg : stmt.objArgs) {
				if (arg.isNonNullObject()) {
					sb.append(objNames.get(arg) + ", ");
				} else if (arg.isNullObject()) {
					sb.append("#NULL, ");
				} else {
					sb.append(stmt.constValues.get(arg.getVariable()).toSMTString() + ", ");
				}
			}
			sb.delete(Math.max(0, sb.length() - 2), sb.length());
			ps.println(sb.toString() + ")");
		}
	}
	
}
