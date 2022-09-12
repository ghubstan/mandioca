package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

import mandioca.bitcoin.stack.Stack;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;
import static mandioca.bitcoin.function.ByteArrayFunctions.intToTruncatedByteArray;

// When talking about scripts, these value-pushing words are usually omitted.

class ConstantOpFunctions extends AbstractOpFunctions {

    static final Supplier<Boolean> opInvalidOpCode = invalidOp;

    // TODO impl case 'OP_IF branch' and return true
    static final Supplier<Boolean> opReserved = invalidOp;

    // TODO impl case 'OP_IF branch' and return true
    static final Supplier<Boolean> opVer = invalidOp;

    static final Supplier<Boolean> opVerIf = invalidOp;

    // TODO impl case 'OP_IF branch' and return true
    static final Supplier<Boolean> opVerNotIf = invalidOp;

    // TODO impl case 'OP_IF branch' and return true
    static final Supplier<Boolean> opReserved1 = invalidOp;

    // TODO impl case 'OP_IF branch' and return true
    static final Supplier<Boolean> opReserved2 = invalidOp;


    static final Function<Stack, Boolean> op0 = (s) -> {
        // An empty array of bytes is pushed onto the stack. (This is not a no-op: an item is added to the stack.)
        // def op_0(stack):
        //    stack.append(encode_num(0))
        //    return True
        s.push(ENC_0);
        return true;
    };


    static final BiFunction<Stack, byte[], Boolean> opPushData = (s, d) -> {
        s.push(d); // TODO Is this supposed to be pushed onto stack?
        return true;
    };

    static final Function<Stack, Boolean> op1Negate = (s) -> {
        s.push((byte) MASK_0xFF);
        return true;
    };


    static final BiFunction<Stack, Integer, Boolean> opPushNumber = (s, i) -> {
        s.push(intToTruncatedByteArray.apply(i));
        return true;
    };

    static final Function<Stack, Boolean> opTrue = (s) -> {
        s.push(ENC_1);
        return true;
    };
}
