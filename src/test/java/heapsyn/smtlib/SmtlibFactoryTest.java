package heapsyn.smtlib;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SmtlibFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public void testVariable() {
		int bvCounter = BoolVariable.getCounter();
		Variable bv1 = new BoolVariable();
		Variable iv1 = new IntVariable("Variable");
		Variable bv2 = new BoolVariable("Variable");
		Variable bv3 = new BoolVariable();
		Variable iv2 = new IntVariable();
		
		assertEquals(SMTSort.SMT_BOOL, bv1.getSMTSort());
		assertEquals(SMTSort.SMT_INT, iv1.getSMTSort());
		assertEquals(BoolVariable.VARNAME_PREFIX + bvCounter, bv1.toSMTString());
		assertEquals("Variable", bv2.toSMTString());
		assertEquals(IntVariable.VARNAME_PREFIX + String.valueOf(IntVariable.getCounter() - 1),
				iv2.toSMTString());
		assertEquals(bvCounter + 3, BoolVariable.getCounter());
		
		assertTrue(iv1.equals(bv2));
		assertTrue(bv3.equals(bv3));
		assertFalse(bv2.equals(null));
		assertEquals(iv1.hashCode(), bv2.hashCode());
		assertNotEquals(bv3.hashCode(), bv1.hashCode());
		
		assertEquals(Collections.singleton(iv2), iv2.getFreeVariables());
		
		Variable cbv = bv1.cloneVariable();
		Variable civ = iv1.cloneVariable();
		assertEquals(cbv.getSMTSort(), bv1.getSMTSort());
		assertEquals(civ.getSMTSort(), iv1.getSMTSort());
		assertNotEquals(cbv.toSMTString(), bv1.toSMTString());
		assertNotEquals(civ.toSMTString(), iv1.toSMTString());
		
		@SuppressWarnings("unused")
		Variable DummyVar = new IntVariable(null);
	}

	@Test
	public void testConstant() {
		Constant bcTrue = new BoolConstant(true);
		Constant bcFalse = new BoolConstant(false);
		Constant icZero = new IntConstant(0);
		Constant icPos = new IntConstant(20201206);
		Constant icNeg = new IntConstant(-1202);
		
		assertEquals(Collections.EMPTY_SET, bcTrue.getFreeVariables());
		assertEquals(Collections.EMPTY_SET, icZero.getFreeVariables());
		assertEquals("Bool", bcFalse.getSMTSort().toSMTString());
		assertEquals("Int", icPos.getSMTSort().toSMTString());
		
		assertEquals("true", bcTrue.toSMTString());
		assertEquals("false", bcFalse.toSMTString());
		assertEquals("0", icZero.toSMTString());
		assertEquals("20201206", icPos.toSMTString());
		assertEquals("-1202", icNeg.toSMTString());
	}
	
	@Test
	public void testExpression() {
		Variable fv1 = new IntVariable("fv1");
		Variable fv2 = new BoolVariable("fv2");
		Variable fv3 = new IntVariable("fv3");
		Constant c1 = new IntConstant(-1);
		Constant c2 = new BoolConstant(false);
		Set<Variable> FVs = new HashSet<>();
		FVs.add(fv1); FVs.add(fv2); FVs.add(fv3);
		
		UnaryExpression u1 = new UnaryExpression(SMTOperator.UNOP_NOT, fv2);
		UnaryExpression u2 = new UnaryExpression(SMTOperator.UNOP_NEG, u1);
		UnaryExpression u3 = new UnaryExpression(SMTOperator.UNOP_NOT, c2);
		BinaryExpression b1 = new BinaryExpression(SMTOperator.BINOP_ADD, fv1, fv3);
		BinaryExpression b2 = new BinaryExpression(SMTOperator.BINOP_SUB, c1, u2);
		BinaryExpression b3 = new BinaryExpression(SMTOperator.BINOP_EQUAL, b2, b1);
		
		assertEquals(SMTSort.SMT_UNKNOWN, u1.getSMTSort());
		assertEquals(SMTSort.SMT_UNKNOWN, b2.getSMTSort());
		assertEquals("(= (- -1 (- (not fv2))) (+ fv1 fv3))", b3.toSMTString());
		assertEquals(Collections.EMPTY_SET, u3.getFreeVariables());
		assertEquals(Collections.singleton(fv2), b2.getFreeVariables());
		assertEquals(FVs, b3.getFreeVariables());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testExistentialProposition() {
		Constant c1 = new BoolConstant(true);
		ExistentialProposition e1 = new ExistentialProposition(null, c1);
		assertEquals(SMTSort.SMT_BOOL, e1.getSMTSort());
		assertEquals("(exists ((DUMMY_VAR Bool) ) true)", e1.toSMTString());
		assertEquals(Collections.EMPTY_SET, e1.getBoundVariables());
		assertEquals(c1, e1.getBody());
		
		Variable v1 = new IntVariable("v1");
		Variable v2 = new IntVariable("v2");
		Variable v3 = new IntVariable("v3");
		Variable v4 = new BoolVariable("v4");
		Set<Variable> V12 = new HashSet<>();
		V12.add(v1); V12.add(v2);
		Set<Variable> V124 = new HashSet<>(V12);
		V124.add(v4);
		Set<Variable> V34 = new HashSet<>();
		V34.add(v3); V34.add(v4);
		
		UnaryExpression u1 = new UnaryExpression(SMTOperator.UNOP_NOT, v4); 
		BinaryExpression b1 = new BinaryExpression(SMTOperator.BINOP_ADD, v1, v2);
		BinaryExpression b2 = new BinaryExpression(SMTOperator.BINOP_EQUAL, b1, v3);
		BinaryExpression b3 = new BinaryExpression(SMTOperator.BINOP_AND, u1, b2);
		
		ExistentialProposition e2 = new ExistentialProposition(V12, b3);
		ExistentialProposition e3 = new ExistentialProposition(Collections.singleton(v4), e2);
		assertEquals(V12, e2.getBoundVariables());
		assertEquals(V124, e3.getBoundVariables());
		assertEquals(b3, e2.getBody());
		assertEquals(b3, e3.getBody());
		assertEquals(V34, e2.getFreeVariables());
		assertEquals(Collections.singleton(v3), e3.getFreeVariables());
		
		ExistentialProposition e4 = new ExistentialProposition(Collections.singleton(v1), b2);
		assertEquals("(exists ((v1 Int) ) (= (+ v1 v2) v3))", e4.toSMTString());
		
		@SuppressWarnings("unused")
		ExistentialProposition DummyProp = new ExistentialProposition(V12, null);
	}
	
	@Test
	public void testGetRenaming1() {
		IntVariable iv = new IntVariable("IV");
		BoolVariable bv = new BoolVariable("BV");
		IntConstant ic = new IntConstant(1);
		UnaryExpression ue = new UnaryExpression(SMTOperator.UNOP_NOT, bv);
		BinaryExpression be1 = new BinaryExpression(SMTOperator.BINOP_SUB, ic, iv);
		BinaryExpression be2 = new BinaryExpression(SMTOperator.BINOP_AND, ue, be1);
		ExistentialProposition ep = new ExistentialProposition(null, be2);
		
		Map<Variable, Variable> vMap = new HashMap<>();
		vMap.put(new IntVariable("IV"), new IntVariable("VI"));
		vMap.put(new BoolVariable("BV"), new BoolVariable("VB"));
		vMap.put(new IntVariable(), new BoolVariable());
		assertEquals("(exists ((DUMMY_VAR Bool) ) (and (not VB) (- 1 VI)))", ep.getRenaming(vMap).toSMTString());
	}
	
	@Test(expected = AssertionError.class)
	public void testGetRenaming2() {
		IntVariable iv = new IntVariable("V");
		BoolConstant bc = new BoolConstant(false);
		Map<Variable, Variable> vMap = new HashMap<>();
		ExistentialProposition ep1 = new ExistentialProposition(Arrays.asList(iv), bc);
		assertEquals("(exists ((V Int) ) false)", ep1.getRenaming(vMap).toSMTString());
		ExistentialProposition ep2 = new ExistentialProposition(Arrays.asList(iv), iv);
		assertEquals("(exists ((V Int) ) V)", ep2.getRenaming(vMap).toSMTString());
		
		vMap.put(new BoolVariable(), new BoolVariable());
		vMap.put(iv, iv);
		ep1.getRenaming(vMap);
	}
	
	@Test
	public void testMultivarExpression() {
		Variable bv1 = new BoolVariable("bv1");
		Variable bv2 = new BoolVariable("bv2");
		Variable iv1 = new BoolVariable("iv1");
		Variable iv2 = new BoolVariable("iv2");
		SMTExpression e1 = bv1;
		SMTExpression e2 = new UnaryExpression(SMTOperator.UNOP_NOT, bv2);
		SMTExpression e3 = new BinaryExpression(SMTOperator.BINOP_EQUAL, iv1, iv2);
		MultivarExpression e = new MultivarExpression(
				SMTOperator.BINOP_AND,
				Arrays.asList(e1, e2, e3)
		);
		
		assertEquals(SMTSort.SMT_UNKNOWN, e.getSMTSort());
		assertEquals("(and bv1 (not bv2) (= iv1 iv2))", e.toSMTString());
		assertEquals(new HashSet<>(Arrays.asList(bv1, bv2, iv1, iv2)), e.getFreeVariables());
		
		Map<Variable, Variable> vMap = new HashMap<>();
		vMap.put(bv1, new IntVariable("X"));
		vMap.put(iv2, new BoolVariable("Y"));
		assertEquals("(and X (not bv2) (= iv1 Y))", e.getRenaming(vMap).toSMTString());
	}
}
