package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.OpCode.*;
import static org.junit.Assert.assertEquals;

// See https://en.bitcoin.it/wiki/Script

public class FlowControlOpFunctionsTest extends OpCodeFunctionsTest {

    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOpNop() {
        doOp(OP_NOP, true);
        assertEquals(0, stack().size());
    }

    @Ignore
    @Test
    public void testOpIf() {  // TODO
        doOp(OP_1, true);
        doOp(OP_IF, true);
    }

    @Ignore
    @Test
    public void testOpNotIf() {  // TODO
        doOp(OP_1, true);
        doOp(OP_NOTIF, true);
    }

    @Ignore
    @Test
    public void testOpElse() {  // TODO
        doOp(OP_1, true);
        doOp(OP_ELSE, true);
    }

    @Ignore
    @Test
    public void testOpEndIf() {  // TODO
        doOp(OP_1, true);
        doOp(OP_ENDIF, true);
    }

    @Test
    public void testOpVerify() {
        doOp(OP_TRUE, true);
        assertEquals(1, stack().size());
        doOp(OP_VERIFY, true);
        assertEquals(0, stack().size());

        doOp(OP_FALSE, true);
        assertEquals(1, stack().size());
        doOp(OP_VERIFY, false);
        assertEquals(0, stack().size());
    }

    @Test
    public void testOpReturn() {
        doOp(OP_TRUE, true);
        assertEquals(1, stack().size());
        doOp(OP_RETURN, false);
        assertEquals(1, stack().size());
    }
}
























