package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.script.processing.ScriptConstants.POSITIVE_ZERO;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

// See https://en.bitcoin.it/wiki/Script

public class ConstantOpFunctionsTest extends OpCodeFunctionsTest {

    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOpFalseAndOp0() {
        doOp(OP_FALSE, true);
        assertEquals(1, stack().size());
        assertArrayEquals(POSITIVE_ZERO, stack().peek());
        doOp(OP_0, true);
        assertEquals(2, stack().size());
        assertArrayEquals(POSITIVE_ZERO, stack().peek());
    }

    @Test
    public void testOp1Negate() {
        doOp(OP_1NEGATE, true);
        assertEquals(1, stack().size());
        assertArrayEquals(B0x0ff, stack().peek());
    }

    @Test
    public void testOpTrueAndOp1() {
        doOp(OP_TRUE, true);
        assertEquals(1, stack().size());
        assertArrayEquals(B0x01, stack().peek());
        doOp(OP_1, true);
        assertEquals(2, stack().size());
        assertArrayEquals(B0x01, stack().peek());
    }

    @Test
    public void testOp2ThroughOp16() {
        doOp(OP_TRUE, true); // make op# == stack.size
        byte[][] nonNegativeSingleByteArrays = {
                B0x00, B0x01, B0x02, B0x03, B0x04, B0x05, B0x06, B0x07, B0x08,
                B0x09, B0x0a, B0x0b, B0x0c, B0x0d, B0x0e, B0x0f, B0x10
        };
        for (int i = 2; i <= 16; i++) {
            OpCode opCode = valueOf("OP_" + i);
            doOp(opCode, true);
            assertEquals(i, stack().size());
            assertArrayEquals(nonNegativeSingleByteArrays[i], stack().peek());
        }
    }
}
