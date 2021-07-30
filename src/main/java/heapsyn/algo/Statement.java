package heapsyn.algo;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import heapsyn.heap.ObjectH;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.Variable;

public class Statement {
	
	private Method javaMethod;
	private List<ObjectH> objArgs;
	private Map<Variable, Constant> constValues;
	private ObjectH returnValue;
	
	public Statement(ObjectH... arguments) {
		this.javaMethod = null;
		this.objArgs = Lists.newArrayList(arguments); 
		this.constValues = new HashMap<>();
		this.returnValue = null;
	}
	
	public Statement(MethodInvoke mInvoke, ObjectH retVal) {
		this.javaMethod = mInvoke.getJavaMethod();
		this.objArgs = mInvoke.getInvokeArguments();
		this.constValues = new HashMap<>();
		this.returnValue = retVal;
	}
	
	public void updateVars(Map<Variable, Constant> vModel) {
		for (ObjectH arg : this.objArgs) {
			if (arg.isHeapObject()) continue;
			Variable var = arg.getVariable();
			Constant val = vModel.get(var);
			if (val != null) {
				this.constValues.put(var, val);
			} else {
				switch (var.getSMTSort()) {
				case INT:
					this.constValues.put(var, IntConst.DEFAULT); break;
				case BOOL:
					this.constValues.put(var, BoolConst.DEFAULT); break;
				}
			}
		}
		if (this.returnValue != null && !this.returnValue.isHeapObject()) {
			Variable var = this.returnValue.getVariable();
			Constant val = vModel.get(var);
			if (val != null) {
				this.constValues.put(var, val);
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
	
	public void updateObjs(Map<ObjectH, ObjectH> objSrc) {
		for (int i = 0; i < this.objArgs.size(); ++i) {
			ObjectH arg = this.objArgs.get(i);
			if (arg.isNonNullObject()) {
				this.objArgs.set(i, objSrc.get(arg));
			}
		}
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
					if (id != 0) {
						if (!createdObjs.containsKey(id)) {
							createdObjs.put(id, "o" + (countRetVals++));
						}
						sb.append(o.getClassH().getJavaClass().getSimpleName() + " ");
						sb.append(createdObjs.get(id) + " = ");
					}
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
			if (!stmt.objArgs.isEmpty()) {
				sb.delete(sb.length() - 2, sb.length());
			}
			ps.println(sb.toString() + ")");
		}
	}
	
}
