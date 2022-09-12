package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.AbstractOpFunctions.encodeNumber;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static org.junit.Assert.assertEquals;

public class ArithmeticFunctionsTest extends OpCodeFunctionsTest {

    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOp1Add() {
        doOp(OP_3, true);
        doOp(OP_5, true);
        doOp(OP_1ADD, true);
        assertEquals(2, stack().size());
        assertPopsMatchElements(
                encodeNumber(6),
                encodeNumber(3)
        );
    }

    @Test
    public void testOp1Sub() {
        doOp(OP_3, true);
        doOp(OP_5, true);
        doOp(OP_1SUB, true);
        assertEquals(2, stack().size());
        assertPopsMatchElements(
                encodeNumber(4),
                encodeNumber(3)
        );
    }

    @Test
    public void testOp2Mul() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_2MUL is disabled");
        doOp(OP_2MUL, true);
    }

    @Test
    public void testOp2Div() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_2DIV is disabled");
        doOp(OP_2DIV, true);
    }

    @Test
    public void testOpNegate() {
        doOp(OP_3, true);
        doOp(OP_5, true);
        doOp(OP_NEGATE, true);
        assertEquals(2, stack().size());
        assertPopsMatchElements(
                encodeNumber(-5),
                encodeNumber(3)
        );
    }

    @Test
    public void testOpAbs() {
        doOp(OP_3, true);
        stack().push(encodeNumber(-13));
        doOp(OP_ABS, true);
        assertEquals(2, stack().size());
        assertPopsMatchElements(
                encodeNumber(13),
                encodeNumber(3)
        );
    }

    @Test
    public void testOpNot() {
        doOp(OP_3, true);
        doOp(OP_NOT, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_0, true);
        doOp(OP_NOT, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_1, true);
        doOp(OP_NOT, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
    }

    @Test
    public void testOp0NotEqual() {
        doOp(OP_3, true);
        doOp(OP_0NOTEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_0, true);
        doOp(OP_0NOTEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_1, true);
        doOp(OP_0NOTEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
    }


    @Test
    public void testOpAdd() {
        doOp(OP_3, true);
        stack().push(encodeNumber(-13));
        doOp(OP_ADD, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(-10);
    }

    @Test
    public void testOpSub() {
        doOp(OP_3, true);
        stack().push(encodeNumber(-13));
        doOp(OP_SUB, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(16); // 3 - (-16) = 16
    }

    @Test
    public void testOpMul() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_MUL is disabled");
        doOp(OP_MUL, true);
    }

    @Test
    public void testOpDiv() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_DIV is disabled");
        doOp(OP_DIV, true);
    }

    @Test
    public void testOpMod() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_MOD is disabled");
        doOp(OP_MOD, true);
    }

    @Test
    public void testOpLShift() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_LSHIFT is disabled");
        doOp(OP_LSHIFT, true);
    }

    @Test
    public void testOpRShift() {
        doOp(OP_3, true);
        doOp(OP_3, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_RSHIFT is disabled");
        doOp(OP_RSHIFT, true);
    }

    @Test
    public void testOpBoolAnd() {
        doOp(OP_3, true);
        doOp(OP_0, true);
        doOp(OP_BOOLAND, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_3, true);
        doOp(OP_13, true);
        doOp(OP_BOOLAND, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
    }

    @Test
    public void testOpBoolOr() {
        doOp(OP_3, true);
        doOp(OP_0, true);
        doOp(OP_BOOLOR, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_0, true);
        doOp(OP_0, true);
        doOp(OP_BOOLOR, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);
    }


    @Test
    public void testOpNumEqual() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_NUMEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_NUMEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
    }

    @Test
    public void testOpNumEqualVerify() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_NUMEQUALVERIFY, false);
        assertEquals(0, stack().size());

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_NUMEQUALVERIFY, true);
        assertEquals(0, stack().size());
    }

    @Test
    public void testOpNumNotEqual() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_NUMNOTEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_NUMNOTEQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);
    }

    @Test
    public void testOpNumLessThan() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_LESSTHAN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_LESSTHAN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_LESSTHAN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);
    }

    @Test
    public void testOpNumGreaterThan() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_GREATERTHAN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_GREATERTHAN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_GREATERTHAN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
    }

    @Test
    public void testOpNumLessThanOrEqual() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_LESSTHANOREQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_LESSTHANOREQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_LESSTHANOREQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);
    }

    @Test
    public void testOpNumGreaterThanOrEqual() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_GREATERTHANOREQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_GREATERTHANOREQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_GREATERTHANOREQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
    }

    @Test
    public void testOpMin() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_MIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(3);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_MIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(3);

        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_MIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(3);
    }

    @Test
    public void testOpMax() {
        doOp(OP_3, true);
        doOp(OP_4, true);
        doOp(OP_MAX, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(4);

        doOp(OP_3, true);
        doOp(OP_3, true);
        doOp(OP_MAX, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(3);

        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_MAX, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(4);
    }

    @Test
    public void testOpWithin() {
        doOp(OP_1, true);   // x < min (fail)
        doOp(OP_2, true);   // min
        doOp(OP_5, true);   // max
        doOp(OP_WITHIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_2, true);   // x = min (pass)
        doOp(OP_2, true);   // min
        doOp(OP_5, true);   // max
        doOp(OP_WITHIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_10, true);  // x between 5,16 (pass)
        doOp(OP_5, true);   // min
        doOp(OP_16, true);  // max
        doOp(OP_WITHIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);

        doOp(OP_5, true);   // x = max (fail)
        doOp(OP_2, true);   // min
        doOp(OP_5, true);   // max
        doOp(OP_WITHIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);

        doOp(OP_10, true);  // x > max (fail)
        doOp(OP_5, true);   // min
        doOp(OP_7, true);   // max
        doOp(OP_WITHIN, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);
    }
}










