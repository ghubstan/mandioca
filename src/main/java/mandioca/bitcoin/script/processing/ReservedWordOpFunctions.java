package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

class ReservedWordOpFunctions {


    // Reserved words
    // Any opcode not assigned is also reserved. Using an unassigned opcode makes the transaction invalid.
    //
    // Word	Opcode	        Hex	    When used...
    // OP_RESERVED	80	    0x50	Transaction is invalid unless occurring in an unexecuted OP_IF branch
    // OP_VER	98	        0x62	Transaction is invalid unless occurring in an unexecuted OP_IF branch
    // OP_VERIF	101	        0x65	Transaction is invalid even when occurring in an unexecuted OP_IF branch
    // OP_VERNOTIF	102	    0x66	Transaction is invalid even when occurring in an unexecuted OP_IF branch
    // OP_RESERVED1	137	    0x89	Transaction is invalid unless occurring in an unexecuted OP_IF branch
    // OP_RESERVED2	138	    0x8a	Transaction is invalid unless occurring in an unexecuted OP_IF branch
    // OP_NOP1, OP_NOP4-OP_NOP10	176, 179-185
    //                      0xb0, 0xb3-0xb9
    //                              The word is ignored. Does not mark transaction as invalid.

}
