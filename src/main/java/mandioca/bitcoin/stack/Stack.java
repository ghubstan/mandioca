package mandioca.bitcoin.stack;

import java.util.EmptyStackException;
import java.util.function.Function;

/**
 * Simple stack interface for implementations using java.util.concurrent Blocking Deque classes instead of
 * java.util.Stack.
 * <p>
 * The stack holds byte vectors. When used as numbers, byte vectors are interpreted as little-endian variable-length
 * integers with the most significant bit determining the sign of the integer. Thus 0x81 represents -1. 0x80 is another
 * representation of zero (so called negative 0). Positive 0 is represented by a null-length vector. Byte vectors are
 * interpreted as Booleans where False is represented by any representation of zero and True is represented by any
 * representation of non-zero.
 * <p>
 * Leading zeros in an integer and negative zero are allowed in blocks but get rejected by the stricter requirements
 * which standard full nodes put on transactions before retransmitting them. Byte vectors on the stack are not allowed
 * to be more than 520 bytes long. Opcodes which take integers and booleans off the stack require that they be no more than
 * 4 bytes long, but addition and subtraction can overflow and result in a 5 byte integer being put on the stack.
 * <p>
 * False is zero or negative zero (using any number of bytes) or an empty array, and True is anything else.
 */
public interface Stack {

    Function<Stack, Boolean> stackIsEmpty = Stack::empty;
    Function<Stack, Boolean> stackIsNotEmpty = (s) -> !stackIsEmpty.apply(s);

    /**
     * Returns the capacity of the stack.
     *
     * @return the capacity of the stack
     */
    int capacity();

    /**
     * Returns the number of elements on the stack. If the stack * contains more than {@code Integer.MAX_VALUE}
     * elements, returns * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements on the stack
     */
    int size();

    /**
     * Pushes a byte element onto the top of this stack.
     *
     * @param element the element to be pushed onto this stack.
     * @return the {@code element} argument.
     */
    byte[] push(byte element);

    /**
     * Pushes a byte array element onto the top of this stack.
     *
     * @param element the element to be pushed onto this stack.
     * @return the {@code element} argument.
     */
    byte[] push(byte[] element);

    /**
     * @param element the element to be placed onto the tail of this stack.
     * @return the {@code element} argument.
     */
    byte[] putLast(byte element);

    /**
     * @param element the element to be placed onto the tail of this stack.
     * @return the {@code element} argument.
     */
    @SuppressWarnings("UnusedReturnValue")
    byte[] putLast(byte[] element);

    /**
     * Removes the object at the top of this stack and returns that object as the value of this function.
     *
     * @return The object at the top of this stack.
     * @throws EmptyStackException if this stack is empty.
     */
    byte[] pop();

    /**
     * Looks at the object at the top of this stack without removing it from the stack.
     *
     * @return the object at the top of this stack.
     * @throws EmptyStackException if this stack is empty.
     */
    byte[] peek();

    /**
     * Looks at the object at the nth element down from the top of this stack, without removing it from the stack.
     * <p>
     * Not a standard stack operation, but provided for convenience.
     *
     * @return the nth object down from the top of this stack.
     * @throws EmptyStackException if this stack is empty.
     */
    byte[] peek(int n);

    /**
     * Removes the first occurrence of the specified element from this stack. If the stack does not contain the element,
     * it is unchanged. More formally, removes the first element {@code e} such that {@code o.equals(e)} (if such an
     * element exists). Returns {@code true} if this deque contained the specified element (or equivalently, if this
     * deque changed as a result of the call).
     *
     * @param o element to be removed from this stack, if present
     * @return {@code true} if this stack changed as a result of the call
     */
    boolean remove(byte[] o);

    /**
     * Tests if this stack is empty.
     *
     * @return {@code true} if and only if this stack contains no elements; {@code false} otherwise.
     */
    boolean empty();

    /**
     * Returns the 1-based position where an object is on this stack. If the object {@code o} occurs as an element in
     * this stack, this method returns the distance from the top of the stack of the occurrence nearest the top of the
     * stack; the topmost element on the stack is considered to be at distance {@code 1}. The {@code equals} method is
     * used to compare {@code o} to the elements in this stack.
     *
     * @param o the desired object.
     * @return the 1-based position from the top of the stack where the object is located; the return value {@code -1}
     * indicates that the object is not on the stack.
     */
    int search(byte[] o);

    int search(byte b);

    /**
     * Atomically removes all of the elements from this stack. The stack will be empty after this call returns.
     */
    void clear();

    /**
     * Returns an array containing all of the elements in this stack, in
     * proper sequence; the runtime type of the returned array is that of
     * the specified array.
     *
     * @param a the array into which the elements of the stack are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this deque
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this deque
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    byte[][] toArray(byte[][] a);

    /**
     * Print stack contents from top to bottom.
     *
     * @param description
     */
    void dumpStack(String description);
}
