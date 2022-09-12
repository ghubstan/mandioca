package mandioca.bitcoin.script.processing;

import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;

/**
 * Script execution stack constants defined in https:github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/script.h
 * <p>
 * The stacks hold byte vectors. When used as numbers, byte vectors are interpreted as little-endian variable-length
 * integers with the most significant bit determining the sign of the integer. Thus 0x81 represents -1. 0x80 is another
 * representation of zero (so called negative 0). Positive 0 is represented by a null-length vector. Byte vectors are
 * interpreted as Booleans where False is represented by any representation of zero and True is represented by any
 * representation of non-zero.
 * <p>
 * Leading zeros in an integer and negative zero are allowed in blocks but get rejected by the stricter requirements
 * which standard full nodes put on transactions before retransmitting them. Byte vectors on the stack are not allowed
 * to be more than 520 bytes long. Opcodes which take integers and booleans off the stack require that they be no more
 * than 4 bytes long, but addition and subtraction can overflow and result in a 5 byte integer being put on the stack.
 */

public class ScriptConstants {

    /**
     * 0x81 represents negative 1
     */
    public static final byte[] NEGATIVE_ONE = new byte[]{(byte) 0x81};
    /**
     * 0x80 represents negative 0
     */
    public static final byte[] NEGATIVE_ZERO = new byte[]{(byte) 0x80};
    /**
     * Null-length vector represents positive 0
     */
    public static final byte[] POSITIVE_ZERO = new byte[]{};
    /**
     * Maximum number of bytes pushable to the stack
     */
    public static final int MAX_SCRIPT_ELEMENT_SIZE = 520;
    /**
     * Maximum number of non-push operations per script
     */
    public static final int MAX_OPS_PER_SCRIPT = 201;
    /**
     * Maximum number of public keys per multisig
     */
    public static final int MAX_PUBKEYS_PER_MULTISIG = 20;
    /**
     * Maximum script length in bytes
     */
    public static final int MAX_SCRIPT_SIZE = 10000;
    /**
     * Maximum number of values on script interpreter stack
     */
    public static final int MAX_STACK_SIZE = 1000;
    /**
     * Threshold for nLockTime: below this value it is interpreted as block number, otherwise as UNIX timestamp.
     */
    public static final int LOCKTIME_THRESHOLD = 500000000;  // Tue Nov  5 00:53:20 1985 UTC
    /**
     * Maximum nLockTime. Since a lock time indicates the last invalid timestamp, a transaction with this lock time will
     * never be valid unless lock time checking is disabled (by setting all input sequence numbers to SEQUENCE_FINAL).
     */
    public static final long LOCKTIME_MAX = 0xffffffffL;
    /**
     * Maximum value that an opcode can be
     */
    public static final int MAX_OPCODE = 0xb9 & MASK_0xFF;
}
