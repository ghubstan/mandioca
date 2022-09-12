package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.stack.Stack;

import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;

// If any opcode marked as disabled is present in a script, it must abort and fail.
// See https://en.bitcoin.it/wiki/Script

public class SpliceOpFunctions extends AbstractOpFunctions {

    static final Supplier<Boolean> opCat = () -> throwingDisabledOp.apply(OP_CAT);
    static final Supplier<Boolean> opSubstr = () -> throwingDisabledOp.apply(OP_SUBSTR);
    static final Supplier<Boolean> opLeft = () -> throwingDisabledOp.apply(OP_LEFT);
    static final Supplier<Boolean> opRight = () -> throwingDisabledOp.apply(OP_RIGHT);

    static final Function<Stack, Boolean> opSize = (s) -> {
        // Pushes the string length of the top element of the stack (without popping it)
        // def op_size(stack):
        //    if len(stack) < 1:
        //        return False
        //    stack.append(encode_num(len(stack[-1])))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            s.push(encodeNumber(s.peek().length));
            return true;
        }
        return false;
    };

}
