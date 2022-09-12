package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.stack.BlockingStack;
import mandioca.bitcoin.stack.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static mandioca.bitcoin.script.processing.Op.isSupportedOpCode;

public final class ScriptStack extends BlockingStack implements Stack {

    private static final Logger log = LoggerFactory.getLogger(ScriptStack.class);

    private static final Function<byte[], String> opCodeOrHex = (e) -> {
        if (e != null && e.length == 1) {
            if (isSupportedOpCode.test(e[0])) {
                return Op.getOpCode.apply(e[0]).name();
                // Commented out because SIGHASH_ALL is same as SUCCESS 0x01
                // } else if (isSigHashType.test(e[0])) {
                //     return getSigHashType.apply(e[0]).name();
            } else {
                return hex.apply(e);
            }
        } else {
            return hex.apply(e);
        }
    };

    public ScriptStack(int capacity) {
        super(capacity);
    }

    public ScriptStack(int capacity, boolean debug, String stackName) {
        super(capacity, debug, stackName);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public byte[] push(byte[] element) {
        if (debug) {
            //  optionalOpCode.apply(element).get()
            String pushInfo = String.format("\t%s%-7s(%s)", stackName, "push", opCodeOrHex.apply(element));
            log.info(pushInfo);
        }
        if (stack.remainingCapacity() == 0) {  // throw exception instead of  blocking
            throw new StackFullException("Cannot push element " + opCodeOrHex.apply(element) + " onto full stack.");
        }
        try {
            stack.putFirst(element);
        } catch (InterruptedException e) {
            throw new RuntimeException((e));
        }
        return element;
    }

    @Override
    public byte[] putLast(byte[] element) {
        if (stack.remainingCapacity() == 0) {  // throw exception instead of  blocking
            throw new StackEmptyException("Cannot put element " + opCodeOrHex.apply(element) + " at tail of full stack.");
        }
        try {
            stack.putLast(element);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return element;
    }

    @Override
    public byte[] pop() {
        checkNotEmpty();
        byte[] element = stack.pop();
        if (debug) {
            String popInfo = String.format("\t%s%-7s(%s)", stackName, "pop", opCodeOrHex.apply(element));
            log.info(popInfo);
        }
        return element;
    }

    @Override
    public byte[] peek() {
        return stack.peek();
    }

    @Override
    public byte[] peek(final int n) {
        if (elementExists.apply(this, n)) {
            throw new NoSuchElementException("There is no element[" + n + "] in stack of size " + stack.size());
        }
        final int[] count = {1}; // The element at the top of the stack is at position 1
        Iterator<byte[]> iterator = stack.iterator(); // iterates in proper sequence, 1st to last
        byte[] current = new byte[]{};
        while (iterator.hasNext()) {
            current = iterator.next();
            if (count[0] == n) {
                if (debug) {
                    String peekInfo = String.format("\t%s%-7s(%d) = '%s'", stackName, "peek", n, opCodeOrHex.apply(current));
                    log.info(peekInfo);
                }
                return current;
            }
            count[0]++;
        }
        if (debug) {
            String peekInfo = String.format("\t%s%-7s(%d) = '%s'", stackName, "peek", n, opCodeOrHex.apply(current));
            log.info(peekInfo);
        }
        return current;
    }


    @Override
    public void dumpStack(String description) {
        StringBuilder descBuilder = new StringBuilder(description).append("\n");
        // Iterates over blocking deque elements in proper sequence, first (head) to last (tail).
        for (byte[] e : stack) {
            descBuilder.append("\t").append(opCodeOrHex.apply(e)).append("\n");
        }
        log.info(descBuilder.toString().trim());
    }
}
