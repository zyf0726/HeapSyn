package heapsyn.wrapper.smt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Sort;
import com.microsoft.z3.Status;
import com.microsoft.z3.Symbol;

import heapsyn.smtlib.ApplyExpr;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.SMTOperator;
import heapsyn.smtlib.SMTSort;
import heapsyn.smtlib.UserFunc;
import heapsyn.smtlib.Variable;

public class Z3JavaAPI implements SMTSolver {
	
	private static Sort convertSort(Context ctx, SMTSort sort) {
		switch (sort) {
		case BOOL:
			return ctx.getBoolSort();
		case INT:
			return ctx.getIntSort();
		}
		return null;
	}
	
	public static SortedSet<UserFunc> getAllUserFunctions(SMTExpression expr) {
		SortedSet<UserFunc> funcs = new TreeSet<>();
		Deque<UserFunc> funcQueue = new ArrayDeque<>();
		funcs.addAll(expr.getUserFunctions());
		funcQueue.addAll(expr.getUserFunctions());
		while (!funcQueue.isEmpty()) {
			UserFunc uf = funcQueue.removeFirst();
			for (UserFunc bodyUf : uf.getBody().getUserFunctions()) {
				if (!funcs.contains(bodyUf)) {
					funcs.add(bodyUf);
					funcQueue.addLast(bodyUf);
				}
			}
		}
		return funcs;
	}
	
	@Override
	public boolean checkSat(SMTExpression constraint, Map<Variable, SMTExpression> model) {
		Context ctx = new Context();
		StringBuilder sb = new StringBuilder();
		List<Symbol> declNames = new ArrayList<>();
		List<FuncDecl> decls = new ArrayList<>();
		for (Variable v : constraint.getFreeVariables()) {
			// sb.append(v.getSMTDecl() + "\n");
			Symbol varSymb = ctx.mkSymbol(v.toSMTString());
			declNames.add(varSymb);
			FuncDecl varDecl = ctx.mkConstDecl(varSymb, convertSort(ctx, v.getSMTSort()));
			decls.add(varDecl);
		}
		for (UserFunc uf : getAllUserFunctions(constraint)) {
			/*
			 *  'define-fun' is semantically equivalent to 'declare-fun' + 'assert forall',
			 *  but is more efficient than the latter.
			 */
			// sb.append(uf.getSMTDecl() + "\n");
			// sb.append(uf.getSMTAssert() + "\n");
			sb.append(uf.getSMTDef() + "\n");
		}
		
		sb.append("(assert " + constraint.toSMTString() + ")\n");
		if (model != null) {
			for (Variable v : Sets.intersection(constraint.getFreeVariables(), model.keySet())) {
				SMTExpression expr = new ApplyExpr(SMTOperator.BIN_EQ, v, model.get(v));
				sb.append("(assert " + expr.toSMTString() + ")\n");
			}
		}
		
		Solver z3Solver = ctx.mkSolver();
		BoolExpr[] es = ctx.parseSMTLIB2String(sb.toString(), null, null,
				declNames.toArray(new Symbol[0]), decls.toArray(new FuncDecl[0]));
		z3Solver.add(es);
		if (z3Solver.check() != Status.SATISFIABLE) {
			ctx.close();
			return false;
		}
		
		if (model != null) {
			Model z3Model = z3Solver.getModel();
			for (Variable var : constraint.getFreeVariables()) {
				Symbol varSymb = ctx.mkSymbol(var.toSMTString());
				FuncDecl varDecl = ctx.mkConstDecl(varSymb, convertSort(ctx, var.getSMTSort()));
				Expr val = z3Model.getConstInterp(varDecl).simplify();
				switch (var.getSMTSort()) {
				case BOOL:
					model.put(var, new BoolConst(((BoolExpr) val).isTrue()));
					break;
				case INT:
					model.put(var, new IntConst(((IntNum) val).getInt64()));
					break;
				}
			}
		}
		ctx.close();
		return true;
	}

}
