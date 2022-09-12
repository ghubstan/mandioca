package mandioca.bitcoin.script.processing;


// See https://en.bitcoin.it/wiki/Script

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.OpCode.OP_CHECKLOCKTIMEVERIFY;
import static mandioca.bitcoin.script.processing.OpCode.OP_CHECKSEQUENCEVERIFY;
import static org.junit.Assert.assertEquals;

public class LocktimeOpFunctionsTest extends OpCodeFunctionsTest {

    @Before
    public void setup() {
        clearStacks();
    }

    @Ignore
    @Test
    public void testOpCheckLocktimeVerify() {       // TODO
        // (previously OP_NOP2)
        // Marks transaction as invalid if the top stack item is greater than the transaction's nLockTime field,
        // otherwise script evaluation continues as though an OP_NOP was executed. Transaction is also invalid if
        // 1. the stack is empty; or 2. the top stack item is negative; or 3. the top stack item is greater than or
        // equal to 500000000 while the transaction's nLockTime field is less than 500000000, or vice versa;
        // or 4. the input's nSequence field is equal to 0xffffffff. The precise semantics are described in BIP 0065.
        doOp(OP_CHECKLOCKTIMEVERIFY, true);
        assertEquals(0, stack().size());
    }

    @Ignore
    @Test
    public void testOpCheckSequenceVerify() {       // TODO
        // (previously OP_NOP3)
        // Marks transaction as invalid if the relative lock time of the input (enforced by BIP 0068 with nSequence)
        // is not equal to or longer than the value of the top stack item. The precise semantics are described in
        // BIP 0112.
        doOp(OP_CHECKSEQUENCEVERIFY, true);
        assertEquals(0, stack().size());
    }

}
