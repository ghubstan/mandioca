package mandioca.bitcoin.stack;

import mandioca.bitcoin.script.processing.StackEmptyException;
import mandioca.bitcoin.script.processing.StackFullException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiFunction;
import java.util.function.Function;

import static mandioca.bitcoin.util.HexUtils.HEX;

public class BlockingStack implements Stack {

    private static final Logger log = LoggerFactory.getLogger(BlockingStack.class);

    protected static final Function<byte[], String> hex = HEX::toPrefixedHexString;

    protected final BiFunction<Stack, Integer, Boolean> elementExists = (s, n) -> s.empty() && s.size() < n;

    protected final int capacity;
    protected final LinkedBlockingDeque<byte[]> stack;
    protected boolean debug;
    protected String stackName;

    public BlockingStack(int capacity) {
        this.capacity = capacity;
        this.stack = new LinkedBlockingDeque<>(capacity); // How big a stack do I need?
        this.debug = false;
        this.stackName = "";
    }

    public BlockingStack(int capacity, boolean debug, String stackName) {
        this.capacity = capacity;
        this.stack = new LinkedBlockingDeque<>(capacity);
        this.debug = debug;
        this.stackName = stackName;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public byte[] push(byte element) {
        return push(new byte[]{element});
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public byte[] push(byte[] element) {
        if (debug) {
            //  optionalOpCode.apply(element).get()
            String pushInfo = String.format("\t%s%-7s(%s)", stackName, "push", hex.apply(element));
            log.info(pushInfo);
        }
        if (stack.remainingCapacity() == 0) {  // throw exception instead of  blocking
            throw new StackFullException("Cannot push element " + hex.apply(element) + " onto full stack.");
        }
        try {
            stack.putFirst(element);
        } catch (InterruptedException e) {
            throw new RuntimeException((e));
        }
        return element;
    }

    @Override
    public byte[] putLast(byte element) {
        return putLast(new byte[]{element});
    }

    @Override
    public byte[] putLast(byte[] element) {
        if (stack.remainingCapacity() == 0) {  // throw exception instead of  blocking
            throw new StackEmptyException("Cannot put element " + hex.apply(element) + " at tail of full stack.");
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
            String popInfo = String.format("\t%s%-7s(%s)", stackName, "pop", hex.apply(element));
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
                    String peekInfo = String.format("\t%s%-7s(%d) = '%s'", stackName, "peek", n, HEX.encode(current));
                    log.info(peekInfo);
                }
                return current;
            }
            count[0]++;
        }
        if (debug) {
            String peekInfo = String.format("\t%s%-7s(%d) = '%s'", stackName, "peek", n, HEX.encode(current));
            log.info(peekInfo);
        }
        return current;
    }


    @Override
    public boolean remove(byte[] o) {
        if (debug) {
            String removeInfo = String.format("\t%s%-7s(%s)", stackName, "remove", hex.apply(o));
            log.info(removeInfo);
        }
        return stack.remove(o);
    }


    @Override
    public boolean empty() {
        return stack.isEmpty();
    }

    @Override
    public int search(byte[] o) {
        String searchInfo = null;
        if (debug) {
            searchInfo = String.format("\t%s%-7s(%s) = ", stackName, "search", hex.apply(o));
        }
        final int[] idx = {1}; // The element at the top of the stack is at position 1
        // Will iterate over elements in the blocking deque in proper sequence, first (head) to last (tail).
        for (byte[] e : stack) {
            if (Arrays.equals(e, o)) {
                if (debug) {
                    searchInfo += "\t" + idx[0];
                }
                return idx[0];
            } else {
                idx[0] += 1;
            }
        }
        if (debug) {
            searchInfo += "\t" + -1;
            log.info(searchInfo);
        }
        return -1;
    }


    @Override
    public int search(byte b) {
        if (debug) {
            String searchInfo = String.format("\t%s%-7s(%s)", stackName, "search", hex.apply(new byte[]{b}));
            log.info(searchInfo);
        }
        return search(new byte[]{b});
    }

    @Override
    public void clear() {
        if (debug) {
            String clearInfo = String.format("\t%s%-7s", stackName, "clear");
            log.info(clearInfo);
        }
        stack.clear();
    }

    @Override
    public byte[][] toArray(byte[][] s) {
        return stack.toArray(s);
    }

    @Override
    public void dumpStack(String description) {
        StringBuilder descBuilder = new StringBuilder(description).append("\n");
        // Iterates over blocking deque elements in proper sequence, first (head) to last (tail).
        for (byte[] e : stack) {
            descBuilder.append("\t").append(hex.apply(e)).append("\n");
        }
        log.info(descBuilder.toString().trim());
    }

    protected void checkNotEmpty() {
        if (empty()) {
            throw new StackEmptyException("Cannot pop empty stack");
        }
    }

}
