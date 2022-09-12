package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.function.ThrowingTriFunction;
import mandioca.bitcoin.stack.Stack;

import java.util.stream.Stream;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.script.processing.AbstractOpFunctions.decodeElement;
import static mandioca.bitcoin.script.processing.OpCodeFunction.mainStack;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

abstract class OpCodeFunctionsTest extends MandiocaTest {

    protected final ThrowingTriFunction<OpCode, Integer, Boolean, Integer> doNTimes = (o, n, e) -> {
        // (o=OcCode, n=N, e=expectedResult)
        int count = 0;
        for (int i = 0; i < n; i++) {
            doOp(o, e);
            count++;
        }
        return count;
    };

    protected static void clearStacks() {
        OpCodeFunction.clearStacks();
    }

    protected static Stack stack() {
        return mainStack();
    }

    protected static Stack altStack() {
        return OpCodeFunction.altStack();
    }

    protected void doOp(OpCode opCode, boolean expectedResult) {
        //noinspection PointlessBooleanExpression
        if (expectedResult == true) {
            assertTrue(OpCodeFunction.doOp(opCode));
        } else {
            assertFalse(OpCodeFunction.doOp(opCode));
        }
    }

    protected void assertPopsMatchElements(final byte[]... elements) {
        Stream.of(elements).forEach(a -> assertArrayEquals(a, stack().pop()));
    }

    protected void assertPopMatchesElement(byte[] element) {
        assertArrayEquals(element, stack().pop());
    }

    protected void assertPopMatchesEncodedNumber(int n) {
        assertEquals(n, decodeElement(stack().pop()));
    }

}
