package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.OpCode.*;

public class ReservedWordOpFunctionsTest extends OpCodeFunctionsTest {

    // Any opcode not assigned is also reserved. Using an unassigned opcode makes the transaction invalid.

    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOpReserved() {
        // Transaction is invalid unless occurring in an unexecuted OP_IF branch
        doOp(OP_RESERVED, false);
        // TODO execute in OP_IF branch, return true
    }

    @Test
    public void testOpVer() {
        // Transaction is invalid unless occurring in an unexecuted OP_IF branch
        doOp(OP_VER, false);
        // TODO execute in OP_IF branch, return true
    }

    @Test
    public void testOpVerIf() {
        // Transaction is invalid *even* when occurring in an unexecuted OP_IF branch
        doOp(OP_VERIF, false);
        // TODO execute in OP_IF branch, return false
    }

    @Test
    public void testOpVerNotIf() {
        // Transaction is invalid *even* when occurring in an unexecuted OP_IF branch
        doOp(OP_VERNOTIF, false);
        // TODO execute in OP_IF branch, return false
    }

    @Test
    public void testOpReserved1() {
        // Transaction is invalid unless occurring in an unexecuted OP_IF branch
        doOp(OP_RESERVED1, false);
        // TODO execute in OP_IF branch, return true
    }

    @Test
    public void testOpReserved2() {
        // Transaction is invalid unless occurring in an unexecuted OP_IF branch
        doOp(OP_RESERVED2, false);
        // TODO execute in OP_IF branch, return true
    }

    @Test
    public void testOpNoOp1ThroughNoOp10() {
        // The word is ignored. Does not mark transaction as invalid.
        doOp(OP_NOP1, true);
        doOp(OP_NOP4, true);
        doOp(OP_NOP5, true);
        doOp(OP_NOP6, true);
        doOp(OP_NOP7, true);
        doOp(OP_NOP8, true);
        doOp(OP_NOP9, true);
        doOp(OP_NOP10, true);
    }
}
