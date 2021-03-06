package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Offsets.X2Y_OFFSET;
import static jbse.common.Type.INT;
import static jbse.common.Type.isPrimitiveOpStack;

import java.util.function.Supplier;

import jbse.dec.DecisionProcedureAlgorithms;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} implementing all the *2* bytecodes 
 * (i2[b/s/l/f/d/c], l2[i/f/d], f2[i/l/d], d2[i/l/f]).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_X2Y extends Algorithm<
BytecodeData_0,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final char fromType; //set by the constructor
    private final char toType; //set by the constructor

    public Algo_X2Y(char fromType, char toType) {
        this.fromType = fromType;
        this.toType = toType;
    }

    private Primitive primitiveTo; //set by cooker

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected Supplier<BytecodeData_0> bytecodeData() {
        return BytecodeData_0::get;
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
                final Primitive primitiveFrom  = (Primitive) this.data.operand(0);
                if (primitiveFrom.getType() != this.fromType) {
                    throwVerifyError(state, this.ctx.getCalculator());
                    exitFromAlgorithm();
                }
                final Calculator calc = this.ctx.getCalculator();
                if (isPrimitiveOpStack(this.toType)) {
                    this.primitiveTo = calc.push(primitiveFrom).to(this.toType).pop();
                } else {
                    //i2b, i2s, i2c case:
                    //must widen to an operand stack type
                    this.primitiveTo = calc.push(primitiveFrom).to(this.toType).widen(INT).pop();
                }
            } catch (ClassCastException | InvalidTypeException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.primitiveTo);
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> X2Y_OFFSET;
    }
}