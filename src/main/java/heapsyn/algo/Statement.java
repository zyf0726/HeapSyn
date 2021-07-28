package heapsyn.algo;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heapsyn.heap.ObjectH;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.Variable;

public class Statement {
	
	public Method javaMethod;
	public List<ObjectH> objArgs;
	public Map<Variable, Constant> constValues;
	public ObjectH returnValue;
	
	public Statement(Map<ObjectH, ObjectH> objSrc,
			Map<Variable, Constant> vModel,	ObjectH... arguments) {
		this.javaMethod = null;
		this.objArgs = new ArrayList<>();
		this.constValues = new HashMap<>();
		this.returnValue = null;
		for (ObjectH arg : arguments) {
			if (arg.isHeapObject()) {
				this.objArgs.add(objSrc.get(arg));
			} else {
				this.objArgs.add(arg);
				Variable var = arg.getVariable();
				Constant constVal = vModel.get(var);
				if (constVal != null) {
					this.constValues.put(var, constVal);
				} else {
					switch (var.getSMTSort()) {
					case INT:
						this.constValues.put(var, IntConst.DEFAULT); break;
					case BOOL:
						this.constValues.put(var, BoolConst.DEFAULT); break;
					}
				}
			}
		}
	}
	
	public Statement(MethodInvoke mInvoke, ObjectH retVal) {
		this.javaMethod = mInvoke.getJavaMethod();
		this.objArgs = mInvoke.getInvokeArguments();
		this.constValues = new HashMap<>();
		this.returnValue = retVal;
	}
	
	public static void printStatements(List<Statement> stmts, PrintStream ps) {
		Map<Integer, String> createdObjs = new HashMap<>();
		Map<ObjectH, String> objNames = new HashMap<>();
		createdObjs.put(0, "#NULL");
		objNames.put(ObjectH.NULL, "#NULL");
		int countRetVals = 0;
		for (Statement stmt : stmts) {
			StringBuilder sb = new StringBuilder();
			if (stmt.returnValue != null) {
				ObjectH o = stmt.returnValue;
				if (o.isNonNullObject()) {
					objNames.put(o, "o" + (countRetVals++));
					sb.append(o.getClassH().getJavaClass().getSimpleName() + " ");
					sb.append(objNames.get(o) + " = ");
				} else if (o.getClassH().isNonNullClass()) {
					Variable idVar = o.getVariable();
					Constant idVal = stmt.constValues.get(idVar);
					Integer id = Integer.parseInt(idVal.toSMTString());
					if (!createdObjs.containsKey(id)) {
						createdObjs.put(id, "o" + (countRetVals++));
					}
					sb.append(o.getClassH().getJavaClass().getSimpleName() + " ");
					sb.append(createdObjs.get(id) + " = ");
				}
			}
			if (stmt.javaMethod != null) {
				sb.append(stmt.javaMethod.getName() + "(");
			} else {
				sb.append("@Test(");
			}
			for (ObjectH arg : stmt.objArgs) {
				if (arg.isHeapObject()) {
					sb.append(objNames.get(arg) + ", ");
				} else {
					Variable var = arg.getVariable();
					Constant val = stmt.constValues.get(var);
					if (arg.getClassH().isJavaClass()) {
						Integer id = Integer.parseInt(val.toSMTString());
						if (createdObjs.containsKey(id)) {
							sb.append(createdObjs.get(id) + ", ");
						} else {
							String type = arg.getClassH().getJavaClass().getSimpleName();
							String name = "o" + (countRetVals++);
							createdObjs.put(id, name);
							ps.println(type + " " + name + " = new " + type + "()");
							sb.append(name + ", ");
						}					
					} else {
						sb.append(val.toSMTString() + ", ");
					}
				}
			}
			sb.delete(Math.max(0, sb.length() - 2), sb.length());
			ps.println(sb.toString() + ")");
		}
	}
	
}
