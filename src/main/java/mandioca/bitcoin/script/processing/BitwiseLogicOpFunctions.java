package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

import mandioca.bitcoin.stack.Stack;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.script.processing.FlowControlOpFunctions.opVerify;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;

// If any opcode marked as disabled is present in a script, it must abort and fail.
// See https://en.bitcoin.it/wiki/Script

class BitwiseLogicOpFunctions extends AbstractOpFunctions {

    static final Supplier<Boolean> opInvert = () -> throwingDisabledOp.apply(OP_INVERT);
    static final Supplier<Boolean> opAnd = () -> throwingDisabledOp.apply(OP_AND);
    static final Supplier<Boolean> opOr = () -> throwingDisabledOp.apply(OP_OR);
    static final Supplier<Boolean> opXOr = () -> throwingDisabledOp.apply(OP_XOR);

    static final Function<Stack, Boolean> opEqual = (s) -> {
        // Returns 1 if the inputs are exactly equal, 0 otherwise
        // def op_equal(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = stack.pop()
        //    element2 = stack.pop()
        //    if element1 == element2:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            if (Arrays.equals(s.pop(), s.pop())) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opEqualVerify = (s) -> {
        // Same as OP_EQUAL, but runs OP_VERIFY afterward
        // def op_equalverify(stack):
        //    return op_equal(stack) and op_verify(stack)
        return opEqual.apply(s) && opVerify.apply(s);
    };

}
