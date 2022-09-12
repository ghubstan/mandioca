package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.OpCode.*;
import static org.junit.Assert.assertEquals;

public class BitwiseLogicFunctionsTest extends OpCodeFunctionsTest {


    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOpCat() {
        doOp(OP_TRUE, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_INVERT is disabled");
        doOp(OP_INVERT, true);
    }

    @Test
    public void testOpAnd() {
        doOp(OP_TRUE, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_AND is disabled");
        doOp(OP_AND, true);
    }

    @Test
    public void testOpOr() {
        doOp(OP_TRUE, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_OR is disabled");
        doOp(OP_OR, true);
    }

    @Test
    public void testOpEqual() {
        doOp(OP_10, true);
        doOp(OP_10, true);
        doOp(OP_EQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(1);
        assertEquals(0, stack().size());

        doOp(OP_9, true);
        doOp(OP_10, true);
        doOp(OP_EQUAL, true);
        assertEquals(1, stack().size());
        assertPopMatchesEncodedNumber(0);
    }

    @Test
    public void testOpEqualVerify() {
        doOp(OP_10, true);
        doOp(OP_10, true);
        doOp(OP_EQUALVERIFY, true);
        assertEquals(0, stack().size());

        doOp(OP_9, true);
        doOp(OP_10, true);
        doOp(OP_EQUALVERIFY, false);
        assertEquals(0, stack().size());
    }
}
