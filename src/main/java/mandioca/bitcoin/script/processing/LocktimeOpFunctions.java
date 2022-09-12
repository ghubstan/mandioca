package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

import mandioca.bitcoin.stack.Stack;

import java.util.function.Function;

import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;

class LocktimeOpFunctions {

    // See https://en.bitcoin.it/wiki/Script

    static final Function<Stack, Boolean> opCheckLocktimeVerify = (s) -> {
        // (previously OP_NOP2)
        //
        // Marks transaction as invalid if the top stack item is greater than the transaction's nLockTime field,
        // otherwise script evaluation continues as though an OP_NOP was executed. Transaction is also invalid if
        // 1. the stack is empty; or 2. the top stack item is negative; or 3. the top stack item is greater than or
        // equal to 500000000 while the transaction's nLockTime field is less than 500000000, or vice versa;
        // or 4. the input's nSequence field is equal to 0xffffffff. The precise semantics are described in BIP 0065.
        //
        // def op_checklocktimeverify(stack, locktime, sequence):
        //    if sequence == 0xffffffff:
        //        return False
        //    if len(stack) < 1:
        //        return False
        //    element = decode_num(stack[-1])
        //    if element < 0:
        //        return False
        //    if element < 500000000 and locktime > 500000000:
        //        return False
        //    if locktime < element:
        //        return False
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };


    static final Function<Stack, Boolean> opCheckSequenceVerify = (s) -> {
        // (previously OP_NOP3)
        // Marks transaction as invalid if the relative lock time of the input (enforced by BIP 0068 with nSequence)
        // is not equal to or longer than the value of the top stack item. The precise semantics are described in
        // BIP 0112
        //
        // def op_checksequenceverify(stack, version, sequence):
        //    if sequence & (1 << 31) == (1 << 31):
        //        return False
        //    if len(stack) < 1:
        //        return False
        //    element = decode_num(stack[-1])
        //    if element < 0:
        //        return False
        //    if element & (1 << 31) == (1 << 31):
        //        if version < 2:
        //            return False
        //        elif sequence & (1 << 31) == (1 << 31):
        //            return False
        //        elif element & (1 << 22) != sequence & (1 << 22):
        //            return False
        //        elif element & 0xffff > sequence & 0xffff:
        //            return False
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };

}
