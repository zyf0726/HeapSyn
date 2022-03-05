package heapsyn.wrapper.smt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

import heapsyn.common.exceptions.UnexpectedInternalException;
import heapsyn.smtlib.BoolConst;
import heapsyn.smtlib.Constant;
import heapsyn.smtlib.ExistExpr;
import heapsyn.smtlib.IntConst;
import heapsyn.smtlib.SMTExpression;
import heapsyn.smtlib.UserFunc;
import heapsyn.smtlib.Variable;

public class ExternalSolver implements IncrSMTSolver {
	
	private File execFile;
	private Path tmpDirPath;
	private int counter;
	
	public ExternalSolver(String execPath, String tmpDirPath) {
		this.execFile = new File(execPath);
		this.tmpDirPath = Paths.get(tmpDirPath);
		this.counter = 0;
	}
	
	private static SortedSet<UserFunc> getAllUserFunctions(SMTExpression expr) {
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
	public boolean checkSat(SMTExpression constraint, Map<Variable, Constant> model) {
		boolean toCheck = (model != null) && (!model.isEmpty());
		File tmpFile = this.tmpDirPath.resolve(Integer.toHexString(constraint.hashCode())
				+ "@" + (this.counter++) + ".z3").toFile();
		long startT = System.currentTimeMillis();
		boolean isSat = this.__checkSat(constraint, model, tmpFile);
		long endT = System.currentTimeMillis();
		tmpFile.delete();
		System.err.print("INFO: invoke external SMT solver to " + (toCheck ? "check" : "solve"));
		System.err.println(", elapsed " + (endT - startT) + "ms");
		return isSat;
	}
	
	private boolean __checkSat(SMTExpression constraint, Map<Variable, Constant> model, File tmpFile) {
		if (model != null) {
			constraint = constraint.getSubstitution(model);
		}
		
		StringBuilder sb = new StringBuilder();
		Set<Variable> FVs = constraint.getFreeVariables();
		Set<UserFunc> UFs = getAllUserFunctions(constraint);
		FVs.forEach(v -> sb.append(v.getSMTDecl() + "\n"));
		UFs.forEach(uf -> sb.append(uf.getSMTDef() + "\n"));
		sb.append("(assert " + constraint.toSMTString() + ")\n");
		sb.append("(check-sat)\n");
		if (!FVs.isEmpty()) {
			sb.append("(get-value ( ");
			FVs.forEach(v -> sb.append(v.toSMTString() + " "));
			sb.append("))\n");
		}
		
		try {
			FileWriter fw = new FileWriter(tmpFile);
			fw.append(sb);
			fw.close();
		} catch (IOException e) {
			throw new UnexpectedInternalException(e);
		}
		
		String command = execFile.getAbsolutePath() + " " + tmpFile.getAbsolutePath();
		Runtime run = Runtime.getRuntime();
		StringBuilder solverOutput = new StringBuilder();
		try {
			Process proc = run.exec(command);
			InputStreamReader procOutput = new InputStreamReader(proc.getInputStream());
			BufferedReader br = new BufferedReader(procOutput);
			if (!br.readLine().equals("sat")) {
				return false;
			}
			if (model == null) {
				return true;
			}
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				solverOutput.append(line);
			}
		} catch (IOException e) {
			throw new UnexpectedInternalException(e);
		}
		
		Map<String, Variable> varTable = new HashMap<>();
		FVs.forEach(o -> varTable.put(o.toSMTString(), o));
		
		List<Character> keywords = Arrays.asList('(', ')', ' ');
		String varName = null, varValue = null;
		boolean isName = true;
		boolean isPositive = true;
		for (int start = 0; start < solverOutput.length(); ++start) {
			char ch = solverOutput.charAt(start);
			if (keywords.contains(ch)) continue;
			if (isName) {
				int end = start;
				while (!keywords.contains(solverOutput.charAt(end)) && end < solverOutput.length())
					++end;
				varName = solverOutput.substring(start, end);
				start = end;
				isName = false;
				isPositive = true;
			} else {
				if (ch == '-') {
					isPositive = false;
					continue;
				}
				int end = start;
				while (!keywords.contains(solverOutput.charAt(end)) && end < solverOutput.length())
					++end;
				if (isPositive) {
					varValue = solverOutput.substring(start, end);
				} else {
					varValue = "-" + solverOutput.substring(start, end);
				}
				start = end;
				isName = true;
				// System.out.println(varName + " " + varValue);
				updateModel(varName, varValue, varTable, model);
			}
		}
		
		return true;
	}
	
	private void updateModel(String varName, String varValue,
			Map<String, Variable> varTable, Map<Variable, Constant> model) {
		Preconditions.checkArgument(varTable.containsKey(varName));
		Variable var = varTable.get(varName);
		if (model.containsKey(var)) return;
		if ("true".equals(varValue)) {
			model.put(var, new BoolConst(true));
		} else if ("false".equals(varValue)) {
			model.put(var, new BoolConst(false));
		} else {
			Long v = null;
			try {
				v = Long.valueOf(varValue);
			} catch (NumberFormatException e) {
				throw new UnexpectedInternalException(e);
			}
			model.put(var, new IntConst(v));
		}
	}
	
	@Override
	public boolean checkSat$pAndNotq(SMTExpression p, ExistExpr q) {
		throw new UnexpectedInternalException("unimplemented api");
	}
	
	@Override
	public boolean checkSatIncr(SMTExpression p) {
		throw new UnexpectedInternalException("unimplemented api");
	}
	
	@Override
	public void closeIncrSolver() {
		throw new UnexpectedInternalException("unimplemented api");
	}
	
	@Override
	public void endPushAssert() {
		throw new UnexpectedInternalException("unimplemented api");
	}
	
	@Override
	public void initIncrSolver() {
		throw new UnexpectedInternalException("unimplemented api");
	}
	
	@Override
	public void pushAssert(SMTExpression p) {
		throw new UnexpectedInternalException("unimplemented api");
		
	}
	
	@Override
	public void pushAssertNot(ExistExpr p) {
		throw new UnexpectedInternalException("unimplemented api");
	}
	
}
