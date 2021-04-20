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
					Variable var = arg.getVariable();
					Constant constVal = stmt.constValues.get(var);
					sb.append(constVal.toSMTString() + ", ");
				}
			}
			sb.delete(Math.max(0, sb.length() - 2), sb.length());
			ps.println(sb.toString() + ")");
		}
	}
	
}
