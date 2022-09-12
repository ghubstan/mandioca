package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static mandioca.bitcoin.script.processing.AbstractOpFunctions.*;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.script.processing.ScriptConstants.*;
import static org.junit.Assert.*;

public class StackOpFunctionsTest extends OpCodeFunctionsTest {

    @Before
    public void setup() {
        clearStacks();
    }

    @Ignore  // TODO check element type (element or operation) before validating length
    // This should be done in the OpCodeFunction.performOp switch, not the stack push method.
    @Test
    public void testPushInvalidNullElement() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Element of length 'null' too short to be pushed onto the stack.  1 is minimum element length allowed.");
        stack().push(null);
    }

    @Ignore  // TODO check element type (element or operation) before validating length
    // This should be done in the OpCodeFunction.performOp switch, not the stack push method.
    @Test
    public void testPushInvalid0LengthElement() {
        byte[] e = new byte[0];
        exception.expect(RuntimeException.class);
        exception.expectMessage("Element of length 0 too short to be pushed onto the stack.  1 is minimum element length allowed.");
        stack().push(e);
    }

    @Ignore  // TODO check element type (element or operation) before validating length
    // This should be done in the OpCodeFunction.performOp switch, not the stack push method.
    @Test
    public void testPushInvalidTooLongElement() {
        byte[] e = new byte[MAX_OPCODE + 1];
        exception.expect(RuntimeException.class);
        exception.expectMessage("Element of length 186 too long to be pushed onto the stack.  185 is maximum element length allowed.");
        stack().push(e);
    }

    @Test
    public void testStackTopIsFalse() {
        doOp(OP_FALSE, true);
        assertTrue(isFalse.apply(stack().pop()));

        stack().push(NEGATIVE_ZERO);
        assertTrue(isFalse.apply(stack().pop()));

        stack().push(POSITIVE_ZERO);
        assertTrue(isFalse.apply(stack().pop()));
    }

    @Test
    public void testStackTopIsTrue() {
        doOp(OP_TRUE, true);
        assertTrue(isTrue.apply(stack().pop()));

        stack().push(NEGATIVE_ONE);
        assertTrue(isTrue.apply(stack().pop()));
    }


    @Test
    public void testOpToAltStack() {
        doNTimes.apply(OP_TRUE, 4, true);
        doNTimes.apply(OP_TOALTSTACK, 2, true);
        assertEquals(2, stack().size());
        assertEquals(2, altStack().size());
    }

    @Test
    public void testOpFromAltStack() {
        doNTimes.apply(OP_TRUE, 4, true);
        doNTimes.apply(OP_TOALTSTACK, 4, true);
        assertEquals(0, stack().size());
        assertEquals(4, altStack().size());
        doNTimes.apply(OP_FROMALTSTACK, 4, true);
        assertEquals(4, stack().size());
        assertEquals(0, altStack().size());
    }

    @Test
    public void testOpIfDup() {
        doNTimes.apply(OP_TRUE, 2, true);
        doOp(OP_IFDUP, true);
        assertEquals(3, stack().size());
    }

    @Test
    public void testOPDepth() {
        doNTimes.apply(OP_TRUE, 5, true);
        doOp(OP_DEPTH, true);
        assertEquals(6, stack().size());
        assertEquals(5, decodeElement(stack().peek()));
    }

    @Test
    public void testOpDrop() {
        doNTimes.apply(OP_TRUE, 2, true);
        assertEquals(2, stack().size());
        doOp(OP_DROP, true);
        assertEquals(1, stack().size());
    }


    @Test
    public void testOpDup() {
        doNTimes.apply(OP_TRUE, 3, true);
        assertEquals(3, stack().size());
        doOp(OP_DUP, true);
        assertEquals(4, stack().size());
        assertArrayEquals(B0x01, stack().peek());
        doNTimes.apply(OP_FALSE, 2, true);
        assertEquals(6, stack().size());
        doOp(OP_DUP, true);
        assertEquals(7, stack().size());
        assertArrayEquals(POSITIVE_ZERO, stack().peek());
    }

    @Test
    public void testOpNip() {
        doOp(OP_FALSE, true);
        doOp(OP_TRUE, true);
        doOp(OP_FALSE, true);
        doOp(OP_TRUE, true);
        assertEquals(4, stack().size());

        doOp(OP_NIP, true);
        assertEquals(3, stack().size());
        assertPopsMatchElements(
                B0x01,
                B0x01,
                POSITIVE_ZERO);
        assertEquals(0, stack().size());
    }

    @Test
    public void testOpOver() {
        doOp(OP_FALSE, true);
        doOp(OP_TRUE, true);
        doOp(OP_FALSE, true);
        assertEquals(3, stack().size());

        doOp(OP_OVER, true);
        assertEquals(4, stack().size());
        assertPopsMatchElements(
                B0x01,
                POSITIVE_ZERO,
                B0x01,
                POSITIVE_ZERO);
        assertEquals(0, stack().size());
    }

    @Test
    public void testOpPick() {
        // TODO this would be more readable if I pushed 1,2,3,... onto test stack instead of true,false,true,...
        for (int i = 0; i < 3; i++) {
            doOp(OP_TRUE, true);
            doOp(OP_FALSE, true);
        }
        doOp(OP_3, true);
        doOp(OP_4, true);
        assertEquals(8, stack().size());

        // copy (move?) element '4' (FALSE) in stack to top
        doOp(OP_PICK, true);
        assertEquals(8, stack().size()); // same size

        // examine the stack, make sure top is POSITIVE_ZERO, 2nd is '3'
        assertPopMatchesElement(POSITIVE_ZERO);
        assertPopMatchesEncodedNumber(3);
        // check the rest of the stack
        assertPopsMatchElements(
                POSITIVE_ZERO,
                B0x01,
                POSITIVE_ZERO,
                B0x01,
                POSITIVE_ZERO,
                B0x01
        );
        assertEquals(0, stack().size());

        // Do it again, but pick for n bigger than stack size.
        doOp(OP_10, true);
        doOp(OP_9, true);
        doOp(OP_PICK, false);
    }

    @Test
    public void testOpRoll() {
        doOp(OP_10, true);
        doOp(OP_9, true);
        doOp(OP_8, true);
        doOp(OP_7, true);
        doOp(OP_6, true);
        doOp(OP_5, true);
        doOp(OP_4, true);
        doOp(OP_3, true);
        assertEquals(8, stack().size());
        //  replace top element with 3rd element in stack '5'
        doOp(OP_ROLL, true);
        assertEquals(7, stack().size());    // one less element after popping top and moving stack[n] to top
        assertPopsMatchElements(                     // examine the stack, 3rd element '5' is now on top
                B0x05,
                B0x04,
                B0x06,
                B0x07,
                B0x08,
                B0x09,
                B0x0a
        );

        // Do it again, but roll for n bigger than stack size.
        doOp(OP_10, true);
        doOp(OP_9, true);
        doOp(OP_ROLL, false);
    }

    @Test
    public void testOpRot() {
        doOp(OP_6, true);
        doOp(OP_5, true);
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(6, stack().size());

        doOp(OP_ROT, true);
        assertEquals(6, stack().size());
        assertPopsMatchElements(            // examine the stack, 4,5,6 are on top, 1,2,3 were rotate to the tail
                encodeNumber(4),
                encodeNumber(5),
                encodeNumber(6),
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(3)
        );
    }

    @Test
    public void testOpSwap() {
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(4, stack().size());

        doOp(OP_SWAP, true);
        assertEquals(4, stack().size());
        assertPopsMatchElements(
                encodeNumber(2),
                encodeNumber(1),
                encodeNumber(3),
                encodeNumber(4)
        );
    }

    @Test
    public void testOpTuck() {
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(4, stack().size());

        doOp(OP_TUCK, true);
        assertEquals(5, stack().size());
        assertPopsMatchElements(
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(1),
                encodeNumber(3),
                encodeNumber(4)
        );
    }

    @Test
    public void testOp2Drop() {
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(4, stack().size());

        doOp(OP_2DROP, true);
        assertEquals(2, stack().size());
        assertPopsMatchElements(
                encodeNumber(3),
                encodeNumber(4)
        );
    }

    @Test
    public void testOp2Dup() {
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(4, stack().size());

        doOp(OP_2DUP, true);
        assertEquals(6, stack().size());
        assertPopsMatchElements(
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(3),
                encodeNumber(4)
        );
    }

    @Test
    public void testOp3Dup() {
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(4, stack().size());

        doOp(OP_3DUP, true);
        assertEquals(7, stack().size());
        assertPopsMatchElements(
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(3),
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(3),
                encodeNumber(4)
        );
    }

    @Test
    public void testOp2Over() {
        doOp(OP_5, true);
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(5, stack().size());

        doOp(OP_2OVER, true);
        assertEquals(7, stack().size());
        assertPopsMatchElements(
                encodeNumber(3),
                encodeNumber(4),
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(3),
                encodeNumber(4),
                encodeNumber(5)
        );
    }

    @Test
    public void testOp2Rot() {
        doOp(OP_7, true);
        doOp(OP_6, true);
        doOp(OP_5, true);
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(7, stack().size());

        doOp(OP_2ROT, true);
        assertEquals(7, stack().size());
        assertPopsMatchElements(
                encodeNumber(5),
                encodeNumber(6),
                encodeNumber(1),
                encodeNumber(2),
                encodeNumber(3),
                encodeNumber(4),
                encodeNumber(7)
        );
    }

    @Test
    public void testOp2Swap() {
        doOp(OP_4, true);
        doOp(OP_3, true);
        doOp(OP_2, true);
        doOp(OP_1, true);
        assertEquals(4, stack().size());

        doOp(OP_2SWAP, true);
        assertEquals(4, stack().size());
        assertPopsMatchElements(
                encodeNumber(3),
                encodeNumber(4),
                encodeNumber(1),
                encodeNumber(2)
        );
    }
}
