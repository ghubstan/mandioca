package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.stack.Stack;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static mandioca.bitcoin.script.processing.OpCode.*;

public class ScriptStackTest extends MandiocaTest {

    @Test
    public void testPushOperations() {
        Stack stack = new ScriptStack(4);
        assertEquals(0, stack.size());

        stack.push(OP_TOALTSTACK.code);
        assertEquals(1, stack.size());
        assertEquals(1, stack.search(OP_TOALTSTACK.code));

        stack.push(OP_DUP.code);
        assertEquals(2, stack.size());
        assertEquals(1, stack.search(OP_DUP.code));
        assertEquals(2, stack.search(OP_TOALTSTACK.code));

        stack.push(OP_VER.code);
        assertEquals(3, stack.size());
        assertEquals(1, stack.search(OP_VER.code));
        assertEquals(2, stack.search(OP_DUP.code));
        assertEquals(3, stack.search(OP_TOALTSTACK.code));

        stack.push(OP_CHECKSIGVERIFY.code);
        assertEquals(4, stack.size());
        assertEquals(1, stack.search(OP_CHECKSIGVERIFY.code));
        assertEquals(2, stack.search(OP_VER.code));
        assertEquals(3, stack.search(OP_DUP.code));
        assertEquals(4, stack.search(OP_TOALTSTACK.code));

        // push over capacity
        exception.expect(StackFullException.class);
        exception.expectMessage("Cannot push element OP_PUSHDATA1 onto full stack.");
        stack.push(OP_PUSHDATA1.code);
    }

    @Test
    public void testPopOperations() {
        Stack stack = new ScriptStack(4);
        stack.push(OP_TOALTSTACK.code);
        stack.push(OP_DUP.code);
        stack.push(OP_VER.code);
        stack.push(OP_CHECKSIGVERIFY.code);

        byte[] element = stack.pop();
        assertEquals(3, stack.size());
        assertEquals(OP_CHECKSIGVERIFY.code, element[0]);

        element = stack.pop();
        assertEquals(2, stack.size());
        assertEquals(OP_VER.code, element[0]);

        stack.push(OP_CHECKSIGVERIFY.code);
        assertEquals(3, stack.size());
        element = stack.pop();
        assertEquals(2, stack.size());
        assertEquals(OP_CHECKSIGVERIFY.code, element[0]);

        stack.pop();
        stack.pop();
        exception.expect(StackEmptyException.class);
        stack.pop();
    }
}
