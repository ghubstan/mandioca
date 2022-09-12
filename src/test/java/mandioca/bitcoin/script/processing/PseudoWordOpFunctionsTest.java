package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PseudoWordOpFunctionsTest extends OpCodeFunctionsTest {

    // Pseudo-words
    // These words are used internally for assisting with transaction matching.
    // They are invalid if used in actual scripts.
    //
    // Word	Opcode	        Hex	    Description
    // OP_PUBKEYHASH	253	0xfd	Represents a public key hashed with OP_HASH160.
    // OP_PUBKEY	    254	0xfe	Represents a public key compatible with OP_CHECKSIG.
    // OP_INVALIDOPCODE	255	0xff	Matches any opcode that is not yet assigned.

    @Before
    public void setup() {
        clearStacks();
    }

    @Ignore
    @Test
    public void testOpPubKeyHash() {            // TODO

    }

    @Ignore
    @Test
    public void testOpPubKey() {                // TODO

    }

    @Ignore
    @Test
    public void testOpInvalidOpCode() {         // TODO

    }
}
