package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Test;

import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.script.processing.AbstractOpFunctions.encodeNumber;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static org.junit.Assert.assertEquals;

public class SpliceOpFunctionsTest extends OpCodeFunctionsTest {

    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOpCat() {
        doNTimes.apply(OP_TRUE, 2, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_CAT is disabled");
        doOp(OP_CAT, true);
    }

    @Test
    public void testOpSubstr() {
        doNTimes.apply(OP_TRUE, 2, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_SUBSTR is disabled");
        doOp(OP_SUBSTR, true);
    }

    @Test
    public void testOpLeft() {
        doNTimes.apply(OP_TRUE, 2, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_LEFT is disabled");
        doOp(OP_LEFT, true);
    }


    @Test
    public void testOpRight() {
        doNTimes.apply(OP_TRUE, 2, true);
        exception.expect(RuntimeException.class);
        exception.expectMessage("OpCode OP_RIGHT is disabled");
        doOp(OP_RIGHT, true);
    }

    @Test
    public void testOpSize() {
        doNTimes.apply(OP_TRUE, 2, true);

        byte[] tenChars = stringToBytes.apply("0123456789");
        stack().push(tenChars);
        doOp(OP_SIZE, true);
        assertEquals(4, stack().size());

        byte[] twentySixChars = stringToBytes.apply("abcdefghijklmnopqrstuvwxyz");
        stack().push(twentySixChars);
        doOp(OP_SIZE, true);
        assertEquals(6, stack().size());

        assertPopsMatchElements(
                encodeNumber(26),
                twentySixChars,
                encodeNumber(10),
                tenChars,
                encodeNumber(1),
                encodeNumber(1)
        );
    }

}
