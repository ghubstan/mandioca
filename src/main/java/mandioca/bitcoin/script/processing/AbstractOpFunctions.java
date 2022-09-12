package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.function.QuadriFunction;
import mandioca.bitcoin.function.ThrowingConsumer;
import mandioca.bitcoin.function.ThrowingFunction;
import mandioca.bitcoin.stack.Stack;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.Math.abs;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.script.processing.Op.getOpCode;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.script.processing.ScriptConstants.*;
import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;
import static mandioca.bitcoin.util.HexUtils.HEX;


abstract class AbstractOpFunctions {

    static final Supplier<Boolean> invalidOp = () -> false;
    static final ThrowingFunction<OpCode, Boolean> throwingDisabledOp = (o) -> {
        // If any opcode marked as disabled is present in a script, it must abort and fail.
        throw new DisabledOpCodeException("OpCode " + o.name() + " is disabled");
    };

    static final BiFunction<Stack, Stack, Boolean> moveTopElementToOtherStack = (s1, s2) -> {
        // Moves top element from stack s1 to stack s2.  Remove element from stack s1.
        if (stackIsNotEmpty.apply(s1)) {
            s2.push(s1.pop());
            return true;
        }
        return false;
    };

    static final BiFunction<Stack, Integer, byte[][]> popNElementsIntoArray = (s, n) -> {
        // Save n elements into an array, populating last array element first so all
        // can be restored to stack in original order by iterating from start of array.
        final byte[][] elements = new byte[n][];
        for (int i = n - 1; i >= 0; i--) {
            elements[i] = s.pop();
        }
        return elements;
    };

    static final QuadriFunction<Stack, byte[][], Integer, Integer, Integer> pushElementsOntoStack = (s, elements, startIdx, endIdx) -> {
        int pushCount = 0;
        for (int i = startIdx; i < endIdx; i++) {
            s.push(elements[i]);
            pushCount++;
        }
        return pushCount;
    };

    static final Predicate<byte[]> isTooShort = (e) -> (e == null) || (e.length == 0);
    static final Predicate<byte[]> isTooLong = (e) -> e.length > MAX_OPCODE;

    static final ThrowingConsumer<byte[]> validateElementLength = (e) -> {
        if (isTooShort.test(e)) {
            throw new InvalidElementLengthException("Element of length "
                    + ((e == null) ? "'null'" : e.length)
                    + " too short to be pushed onto the stack.  1 is minimum element length allowed.");
        } else if (isTooLong.test((e))) {
            throw new InvalidElementLengthException("Element of length "
                    + e.length
                    + " too long to be pushed onto the stack.  " + MAX_OPCODE + " is maximum element length allowed.");
        }
    };

    static final Function<byte[], byte[]> appendNegativeZero = (b) -> concatenate.apply(b, NEGATIVE_ZERO);
    static final Function<byte[], byte[]> appendZero = (b) -> concatenate.apply(b, ZERO_BYTE);
    static final BiFunction<byte[], Integer, byte[]> appendAbsN = (b, a) -> concatenate.apply(b, new byte[]{(byte) (a & MASK_0xFF)});
    static final Function<Byte, Boolean> negativeZeroBitwiseAndEqualTrue = (b) -> (NEGATIVE_ZERO[0] & b) != 0;
    static final Function<byte[], Boolean> isFalse = (e) -> decodeElement(e) == 0;  // F is any representation of zero
    static final Function<byte[], Boolean> isTrue = (e) -> decodeElement(e) != 0;   // T is any representation of non-zero
    static final Function<Byte, Byte> negativeZeroBitwiseOr = (b) -> (byte) (NEGATIVE_ZERO[0] | b);
    static final BiFunction<byte[], Integer, byte[]> appendAbsoluteNs = (r, n) -> {
        int absN = abs(n);
        while (absN != 0) {
            r = appendAbsN.apply(r, absN);
            absN >>= 8;
        }
        return r;
    };


    static final byte[] ENC_0 = encodeNumber(0);
    static final byte[] ENC_1 = encodeNumber(1);

    static final Predicate<byte[]> isEncodedZero = (b) -> Arrays.equals(b, ENC_0);

    /*
    def encode_num(num):
        if num == 0:
            return b''
        abs_num = abs(num)
        negative = num < 0
        result = bytearray()
        while abs_num:
            result.append(abs_num & 0xff)
            abs_num >>= 8
        if result[-1] & 0x80:     # result[-1] is last item in the array
            if negative:
                result.append(0x80)
            else:
                result.append(0)
        elif negative:
            result[-1] |= 0x80
        return bytes(result)
     */
    static byte[] encodeNumber(int n) {
        byte[] result = POSITIVE_ZERO;
        if (n == 0) {
            return result;
        }
        result = appendAbsoluteNs.apply(result, n);
        boolean negative = n < 0;
        byte lastByte = result[result.length - 1];
        if (negativeZeroBitwiseAndEqualTrue.apply(lastByte)) {
            result = negative ? appendNegativeZero.apply(result) : appendZero.apply(result);
        } else if (negative) {
            result[result.length - 1] = negativeZeroBitwiseOr.apply(lastByte);
        }
        return result;
    }

    /*
    def decode_num(element):
        if element == b'':
            return 0
        big_endian = element[::-1]      // a[::-1] -> all items in the array, reversed
        if big_endian[0] & 0x80:        // True = 1, False = 0
            negative = True
            result = big_endian[0] & 0x7f
        else:
            negative = False
            result = big_endian[0]
        for c in big_endian[1:]:      // 0 based, a[1:] means everything except a[0]
            result <<= 8
            result += c
        if negative:
            return -result
        else:
            return result
     */
    static int decodeElement(byte[] element) {
        if (isZero.test(element)) {
            return 0;
        }
        //
        byte[] beBytes = reverse.apply(element);
        boolean negative = negativeZeroBitwiseAndEqualTrue.apply(beBytes[0]);
        int result = negative ? beBytes[0] & 0x7f : beBytes[0];
        for (int i = 1; i < beBytes.length; i++) {
            result <<= 8;
            result += beBytes[i] & MASK_0xFF;
        }
        return negative ? -result : result;
    }

    static int decodeOpCodeN(OpCode opCode) {
        if (opCode.equals(OP_0)) {
            return 0;
        }
        if (((int) opCode.code & MASK_0xFF) > ((int) OP_NOP10.code & MASK_0xFF)) {
            throw new RuntimeException("OpCode " + opCode.name()
                    + " is larger than MAX_OPCODE " + HEX.byteToPrefixedHex.apply((byte) MAX_OPCODE));
        }
        return ((int) opCode.code & MASK_0xFF) - (((int) OP_1.code & MASK_0xFF) - 1);
    }

    static OpCode encodeOpCodeN(int n) {
        if (n < 0 || n > MAX_OPCODE) {
            throw new RuntimeException("Number " + n
                    + " is outside valid OP_N range (0x00, " + HEX.byteToPrefixedHex.apply((byte) MAX_OPCODE) + ")");
        }
        if (n == 0) {
            return OP_0;
        }
        return getOpCode.apply((byte) n);
    }
}
