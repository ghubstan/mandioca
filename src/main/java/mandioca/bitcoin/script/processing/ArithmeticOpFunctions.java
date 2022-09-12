package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

import mandioca.bitcoin.stack.Stack;

import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.script.processing.FlowControlOpFunctions.opVerify;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;

// Note: Arithmetic inputs are limited to signed 32-bit integers, but may overflow their output.
//
// If any input value for any of these commands is longer than 4 bytes, the script must abort and fail.
// If any opcode marked as disabled is present in a script - it must also abort and fail.
// See https://en.bitcoin.it/wiki/Script

class ArithmeticOpFunctions extends AbstractOpFunctions {

    static final Function<Stack, Boolean> op1Add = (s) -> {
        // 1 is added to the input
        // def op_1add(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = decode_num(stack.pop())
        //    stack.append(encode_num(element + 1))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            s.push(encodeNumber(decodeElement(s.pop()) + 1));
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op1Sub = (s) -> {
        // 1 is subtracted from the input.
        // def op_1sub(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = decode_num(stack.pop())
        //    stack.append(encode_num(element - 1))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            s.push(encodeNumber(decodeElement(s.pop()) - 1));
            return true;
        }
        return false;
    };


    static final Supplier<Boolean> op2Mul = () -> throwingDisabledOp.apply(OP_2MUL);
    static final Supplier<Boolean> op2Div = () -> throwingDisabledOp.apply(OP_2DIV);


    static final Function<Stack, Boolean> opNegate = (s) -> {
        // The sign of the input is flipped
        // def op_negate(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = decode_num(stack.pop())
        //    stack.append(encode_num(-element))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            s.push(encodeNumber(-decodeElement(s.pop())));
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> opAbs = (s) -> {
        // The input is made positive
        // def op_abs(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = decode_num(stack.pop())
        //    if element < 0:
        //        stack.append(encode_num(-element))
        //    else:
        //        stack.append(encode_num(element))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            int n = decodeElement(s.pop());
            if (n < 0) {
                s.push(encodeNumber(-n));
            } else {
                s.push(encodeNumber(n));
            }
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opNot = (s) -> {
        // If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
        // def op_not(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = stack.pop()
        //    if decode_num(element) == 0:   // TODO bug?   does not check if e == 1, which means flip
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            int n = decodeElement(s.pop());
            if (n == 0 || n == 1) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> op0NotEqual = (s) -> {
        // TODO fix broken decodeNum & encodeNum & test
        // Returns 0 if the input is 0. 1 otherwise.
        // def op_0notequal(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = stack.pop()
        //    if decode_num(element) == 0:
        //        stack.append(encode_num(0))
        //    else:
        //        stack.append(encode_num(1))
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            if (decodeElement(s.pop()) == 0) {
                s.push(ENC_0);
            } else {
                s.push(ENC_1);
            }
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opAdd = (s) -> {
        // a is added to b
        // def op_add(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    stack.append(encode_num(element1 + element2))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            s.push(encodeNumber(decodeElement(s.pop()) + decodeElement(s.pop())));
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> opSub = (s) -> {
        // b is subtracted from a
        // def op_sub(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    stack.append(encode_num(element2 - element1))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            s.push(encodeNumber(a - b));
            return true;
        }
        return false;
    };

    static final Supplier<Boolean> opMul = () -> throwingDisabledOp.apply(OP_MUL);
    static final Supplier<Boolean> opDiv = () -> throwingDisabledOp.apply(OP_DIV);
    static final Supplier<Boolean> opMod = () -> throwingDisabledOp.apply(OP_MOD);
    static final Supplier<Boolean> opLShift = () -> throwingDisabledOp.apply(OP_LSHIFT);
    static final Supplier<Boolean> opRShift = () -> throwingDisabledOp.apply(OP_RSHIFT);

    static final Function<Stack, Boolean> opBoolAnd = (s) -> {
        // If both a and b are not 0, the output is 1. Otherwise 0.
        // def op_booland(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    if element1 and element2:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (a != 0 && b != 0) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };
    static final Function<Stack, Boolean> opBoolOr = (s) -> {
        // If a or b is not 0, the output is 1. Otherwise 0.
        // def op_boolor(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    if element1 or element2:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (a != 0 || a != b) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opNumEqual = (s) -> {
        // Returns 1 if the numbers are equal, 0 otherwise.
        // def op_numequal(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    if element1 == element2:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            if (decodeElement(s.pop()) == decodeElement(s.pop())) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opNumEqualVerify = (s) -> {
        // Same as OP_NUMEQUAL, but runs OP_VERIFY afterward
        // def op_numequalverify(stack):
        //    return op_numequal(stack) and op_verify(stack)
        return opNumEqual.apply(s) && opVerify.apply(s);
    };


    static final Function<Stack, Boolean> opNumNotEqual = (s) -> {
        // Returns 1 if the numbers are not equal, 0 otherwise
        // def op_numnotequal(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    if element1 == element2:
        //        stack.append(encode_num(0))
        //    else:
        //        stack.append(encode_num(1))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            if (decodeElement(s.pop()) == decodeElement(s.pop())) {
                s.push(ENC_0);
            } else {
                s.push(ENC_1);
            }
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> opLessThan = (s) -> {
        // Returns 1 if a is less than b, 0 otherwise
        // def op_lessthan(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop()) // b
        //    element2 = decode_num(stack.pop()) // a
        //    if element2 < element1:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (a < b) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };
    static final Function<Stack, Boolean> opGreaterThan = (s) -> {
        // Returns 1 if a is greater than b, 0 otherwise
        // def op_greaterthan(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop()) // b
        //    element2 = decode_num(stack.pop()) // a
        //    if element2 > element1:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (a > b) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };
    static final Function<Stack, Boolean> opLessThanOrEqual = (s) -> {
        // Returns 1 if a is less than or equal to b, 0 otherwise
        // def op_lessthanorequal(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop()) // b
        //    element2 = decode_num(stack.pop()) // a
        //    if element2 <= element1:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (a <= b) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };
    static final Function<Stack, Boolean> opGreaterThanOrEqual = (s) -> {
        // Returns 1 if a is greater than or equal to b, 0 otherwise.
        // def op_greaterthanorequal(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop()) // b
        //    element2 = decode_num(stack.pop()) // a
        //    if element2 >= element1:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (a >= b) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };
    static final Function<Stack, Boolean> opMin = (s) -> {
        // Returns the smaller of a and b
        // def op_min(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    if element1 < element2:
        //        stack.append(encode_num(element1))
        //    else:
        //        stack.append(encode_num(element2))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (b < a) {
                s.push(encodeNumber(b));
            } else {
                s.push(encodeNumber(a));
            }
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> opMax = (s) -> {
        // Returns the larger of a and b
        // def op_max(stack):
        //    if len(stack) < 2:
        //        return False
        //    element1 = decode_num(stack.pop())
        //    element2 = decode_num(stack.pop())
        //    if element1 > element2:
        //        stack.append(encode_num(element1))
        //    else:
        //        stack.append(encode_num(element2))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            int b = decodeElement(s.pop());
            int a = decodeElement(s.pop());
            if (b > a) {
                s.push(encodeNumber(b));
            } else {
                s.push(encodeNumber(a));
            }
            return true;
        }
        return false;
    };


    static final Function<Stack, Boolean> opWithin = (s) -> {
        // Returns 1 if x is within the specified range (left-inclusive, right-exclusive), 0 otherwise
        // def op_within(stack):
        //    if len(stack) < 3:
        //        return False
        //    maximum = decode_num(stack.pop())
        //    minimum = decode_num(stack.pop())
        //    element = decode_num(stack.pop())
        //    if element >= minimum and element < maximum:
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //    return True
        if (stackIsNotEmpty.apply(s) && s.size() >= 3) {
            int max = decodeElement(s.pop());
            int min = decodeElement(s.pop());
            int x = decodeElement(s.pop());
            if (x >= min && x < max) {
                s.push(ENC_1);
            } else {
                s.push(ENC_0);
            }
            return true;
        }
        return false;
    };
}
