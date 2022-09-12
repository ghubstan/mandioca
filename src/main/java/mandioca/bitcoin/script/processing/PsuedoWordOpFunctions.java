package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

class PsuedoWordOpFunctions {

    // Pseudo-words
    // These words are used internally for assisting with transaction matching.
    // They are invalid if used in actual scripts.
    //
    // Word	Opcode	        Hex	    Description
    // OP_PUBKEYHASH	253	0xfd	Represents a public key hashed with OP_HASH160.
    // OP_PUBKEY	    254	0xfe	Represents a public key compatible with OP_CHECKSIG.
    // OP_INVALIDOPCODE	255	0xff	Matches any opcode that is not yet assigned.
}
