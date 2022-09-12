package mandioca.bitcoin.script.processing;

/*
From: https://bitcoin.stackexchange.com/questions/72471/getrawtransaction-asm-returns-with-all-should-it-be
The representation of data is from site to site different.
The hexcode "01" is a SIGHASH ALL Opcode, defined here:
https://en.bitcoin.it/wiki/Contract#Theory

I show here all the opcodes and their definition of your example.
Basically sigscript of this example consists of.
The signature is an ASN1 DER structure, followed by the public key
as hex chars. Drilling deeper, it gets this:

49: OP_DATA_0x49:        push hex 49 (decimal 73) bytes on stack
30: OP_SEQUENCE_0x30:    type tag indicating SEQUENCE, begin sigscript
46: OP_LENGTH_0x46:      length of R + S
02: OP_INT_0x02:         type tag INTEGER indicating length
21: OP_LENGTH_0x21:      this is SIG R (33 Bytes)
02: OP_INT_0x02:         type tag INTEGER indicating length
21: OP_LENGTH_0x21:      this is SIG S (33 Bytes)
01: OP_SIGHASHALL:       this terminates the ECDSA signature (ASN1-DER structure)
21: OP_DATA_0x21:        length compressed Public Key (X9.63 form, 33 Bytes)

So [ALL] and "01" indicate the same. One format is showing the OpCodes
(see also in the scriptPubKey section),
one is plain hex representation, to make cut&paste easy.

OpCodes doc is here: https://en.bitcoin.it/wiki/Script
OpCode src https://github.com/bitcoin/bitcoin/tree/v0.18.1/src
https://github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/script.h
https://github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/script.cpp
*/

import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;
import static mandioca.bitcoin.script.processing.ArithmeticOpFunctions.*;
import static mandioca.bitcoin.script.processing.BitwiseLogicOpFunctions.*;
import static mandioca.bitcoin.script.processing.ConstantOpFunctions.*;
import static mandioca.bitcoin.script.processing.CryptoOpFunctions.*;
import static mandioca.bitcoin.script.processing.FlowControlOpFunctions.*;
import static mandioca.bitcoin.script.processing.LocktimeOpFunctions.opCheckLocktimeVerify;
import static mandioca.bitcoin.script.processing.LocktimeOpFunctions.opCheckSequenceVerify;
import static mandioca.bitcoin.script.processing.SpliceOpFunctions.*;
import static mandioca.bitcoin.script.processing.StackOpFunctions.*;

// See https://en.bitcoin.it/wiki/Script

// Removed opcodes are sometimes said to be "disabled", but this is something of a misnomer because there
// is absolutely no way for anyone using Bitcoin to use these opcodes (they simply do not exist anymore
// in the protocol), and there are also no solid plans to ever re-enable all of these opcodes.
// They are included here for historical interest only.

public enum OpCode {

    // Op Code Types defined in https://github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/script.h

    // pushvalue codes
    OP_FALSE((byte) 0x00, "0", op0, true,
            "An empty array of 4 bytes is pushed onto the stack. (This is not a no-op; an item is added to the stack.)"),
    OP_0((byte) 0x00, "0", op0, true,
            "TODO fix bug & comment (POSITIVE_ZERO is pushed).  An empty array of 4 bytes is pushed onto the stack. (This is not a no-op; an item is added to the stack.)"),

    OP_PUSHDATA1((byte) 0x4c, "OP_PUSHDATA1", opPushData, true,
            "The next byte contains the number of bytes to be pushed onto the stack"),
    OP_PUSHDATA2((byte) 0x4d, "OP_PUSHDATA2", opPushData, true,
            "The next two bytes contain the number of bytes to be pushed onto the stack in little endian order"),
    OP_PUSHDATA4((byte) 0x4e, "OP_PUSHDATA4", opPushData, true,
            "The next four bytes contain the number of bytes to be pushed onto the stack in little endian order"),

    OP_1NEGATE((byte) 0x4f, "-1", op1Negate, true,
            "The number -1 is pushed onto the stack"),
    OP_RESERVED((byte) 0x50, "OP_RESERVED", opReserved, true,
            "Transaction is invalid unless occurring in an unexecuted OP_IF branch"),

    OP_1((byte) 0x51, "1", opPushNumber, true, "The number 1 is pushed onto the stack"),
    OP_TRUE((byte) 0x51, "1", opTrue, true, "The number 1 is pushed onto the stack"),

    OP_2((byte) 0x52, "2", opPushNumber, true, "The number 2 is pushed onto the stack"),
    OP_3((byte) 0x53, "3", opPushNumber, true, "The number 3 is pushed onto the stack"),
    OP_4((byte) 0x54, "4", opPushNumber, true, "The number 4 is pushed onto the stack"),
    OP_5((byte) 0x55, "5", opPushNumber, true, "The number 5 is pushed onto the stack"),
    OP_6((byte) 0x56, "6", opPushNumber, true, "The number 6 is pushed onto the stack"),
    OP_7((byte) 0x57, "7", opPushNumber, true, "The number 7 is pushed onto the stack"),
    OP_8((byte) 0x58, "8", opPushNumber, true, "The number 8 is pushed onto the stack"),
    OP_9((byte) 0x59, "9", opPushNumber, true, "The number 9 is pushed onto the stack"),
    OP_10((byte) 0x5a, "10", opPushNumber, true, "The number 10 is pushed onto the stack"),
    OP_11((byte) 0x5b, "11", opPushNumber, true, "The number 11 is pushed onto the stack"),
    OP_12((byte) 0x5c, "12", opPushNumber, true, "The number 12 is pushed onto the stack"),
    OP_13((byte) 0x5d, "13", opPushNumber, true, "The number 13 is pushed onto the stack"),
    OP_14((byte) 0x5e, "14", opPushNumber, true, "The number 14 is pushed onto the stack"),
    OP_15((byte) 0x5f, "15", opPushNumber, true, "The number 15 is pushed onto the stack"),
    OP_16((byte) 0x60, "16", opPushNumber, true, "The number 16 is pushed onto the stack"),

    // control codes

    OP_NOP((byte) 0x61, "OP_NOP", opNoOp, true, "Does nothing"),
    OP_VER((byte) 0x62, "OP_VER", opVer, true,
            "Transaction is invalid unless occurring in an unexecuted OP_IF branch"),
    OP_IF((byte) 0x63, "OP_IF", opIf, true,
            "TODO If the top stack value is not False, the statements are executed. The top stack value is removed."),
    OP_NOTIF((byte) 0x64, "OP_NOTIF", opNotIf, true,
            "TODO If the top stack value is False, the statements are executed. The top stack value is removed."),
    OP_VERIF((byte) 0x65, "OP_VERIF", opVerIf, true,
            "Transaction is invalid even when occurring in an unexecuted OP_IF branch"),
    OP_VERNOTIF((byte) 0x66, "OP_VERNOTIF", opVerNotIf, true,
            "Transaction is invalid even when occurring in an unexecuted OP_IF branch"),
    OP_ELSE((byte) 0x67, "OP_ELSE", opElse, true,
            "TODO If the preceding OP_IF or OP_NOTIF or OP_ELSE was not executed then these statements are and if the preceding OP_IF or OP_NOTIF or OP_ELSE was executed then these statements are not"),
    OP_ENDIF((byte) 0x68, "OP_ENDIF", opEndIf, true,
            "TODO Ends an if/else block. All blocks must end, or the transaction is invalid. An OP_ENDIF without OP_IF earlier is also invalid."),
    OP_VERIFY((byte) 0x69, "OP_VERIFY", opVerify, true,
            "Marks transaction as invalid if top stack value is not true. The top stack value is removed"),
    OP_RETURN((byte) 0x6a, "OP_RETURN", opReturn, true,
            "Marks transaction as invalid. Since bitcoin 0.9, a standard way of attaching extra data to "
                    + "transactions is to add a zero-value output with a scriptPubKey consisting of OP_RETURN followed by "
                    + "data.  Such outputs are provably unspendable and specially discarded from storage in the UTXO set, "
                    + "reducing their cost to the network. Since 0.12, standard relay rules allow a single output with "
                    + "OP_RETURN, that contains any sequence of push statements (or OP_RESERVED[1]) after the OP_RETURN "
                    + "provided the total scriptPubKey length is at most 83 bytes"),

    // stack ops codes

    OP_TOALTSTACK((byte) 0x6b, "OP_TOALTSTACK", opToAltStack, true,
            "Puts the input onto the top of the alt stack. Removes it from the main stack"),
    OP_FROMALTSTACK((byte) 0x6c, "OP_FROMALTSTACK", opFromAltStack, true,
            "Puts the input onto the top of the main stack. Removes it from the alt stack"),
    OP_2DROP((byte) 0x6d, "OP_2DROP", op2Drop, true, "Removes the top two stack items"),
    OP_2DUP((byte) 0x6e, "OP_2DUP", op2Dup, true, "Duplicates the top two stack items"),
    OP_3DUP((byte) 0x6f, "OP_3DUP", op3Dup, true, "Duplicates the top three stack items"),
    OP_2OVER((byte) 0x70, "OP_2OVER", op2Over, true,
            "Copies the pair of items two spaces back in the stack to the front"),
    OP_2ROT((byte) 0x71, "OP_2ROT", op2Rot, true,
            "The fifth and sixth items back are moved to the top of the stack"),
    OP_2SWAP((byte) 0x72, "OP_2SWAP", op2Swap, true, "Swaps the top two pairs of items"),
    OP_IFDUP((byte) 0x73, "OP_IFDUP", opIfDup, true, "If the top stack value is not 0, duplicate it"),
    OP_DEPTH((byte) 0x74, "OP_DEPTH", opDepth, true, "Puts the number of stack items onto the stack"),
    OP_DROP((byte) 0x75, "OP_DROP", opDrop, true, "Removes the top stack item"),
    OP_DUP((byte) 0x76, "OP_DUP", opDup, true, "Push duplicate of top element onto stack"),
    OP_NIP((byte) 0x77, "OP_NIP", opNip, true, "Removes the second-to-top stack item"),
    OP_OVER((byte) 0x78, "OP_OVER", opOver, true, "Copies the second-to-top stack item to the top"),
    OP_PICK((byte) 0x79, "OP_PICK", opPick, true, "The item n back in the stack is copied to the top"),
    OP_ROLL((byte) 0x7a, "OP_ROLL", opRoll, true, "The item n back in the stack is moved to the top"),
    OP_ROT((byte) 0x7b, "OP_ROT", opRot, true, "The top three items on the stack are rotated to the left"),
    OP_SWAP((byte) 0x7c, "OP_SWAP", opSwap, true, "The top two items on the stack are swapped"),
    OP_TUCK((byte) 0x7d, "OP_TUCK", opTuck, true,
            "The item at the top of the stack is copied and inserted before the second-to-top item"),

    // splice op codes

    OP_CAT((byte) 0x7e, "OP_CAT", opCat, false, "Concatenates two strings. disabled"),
    OP_SUBSTR((byte) 0x7f, "OP_SUBSTR", opSubstr, false, "Returns a section of a string. disabled"),
    OP_LEFT((byte) 0x80, "OP_LEFT", opLeft, false,
            "Keeps only characters left of the specified point in a string. disabled"),
    OP_RIGHT((byte) 0x81, "OP_RIGHT", opRight, false,
            "Keeps only characters right of the specified point in a string. disabled"),
    OP_SIZE((byte) 0x82, "OP_SIZE", opSize, true,
            "Pushes the string length of the top element of the stack (without popping it)"),

    // bit logic codes

    OP_INVERT((byte) 0x83, "OP_INVERT", opInvert, false,
            "Flips all of the bits in the input. disabled"),
    OP_AND((byte) 0x84, "OP_AND", opAnd, false,
            "Boolean and between each bit in the inputs. disabled"),
    OP_OR((byte) 0x84, "OP_OR", opOr, false,
            "Boolean or between each bit in the inputs. disabled"),
    OP_XOR((byte) 0x84, "OP_XOR", opXOr, false,
            "Boolean exclusive or between each bit in the inputs. disabled"),
    OP_EQUAL((byte) 0x87, "OP_EQUAL", opEqual, true,
            "Returns 1 if the inputs are exactly equal, 0 otherwise"),
    OP_EQUALVERIFY((byte) 0x88, "OP_EQUALVERIFY", opEqualVerify, true,
            "Same as OP_EQUAL, but runs OP_VERIFY afterward"),
    OP_RESERVED1((byte) 0x89, "OP_RESERVED1", opReserved1, true,
            "Transaction is invalid unless occurring in an unexecuted OP_IF branch"),
    OP_RESERVED2((byte) 0x8a, "OP_RESERVED2", opReserved2, true,
            "Transaction is invalid unless occurring in an unexecuted OP_IF branch"),

    // numeric codes

    OP_1ADD((byte) 0x8b, "OP_1ADD", op1Add, true, "1 is added to the input"),
    OP_1SUB((byte) 0x8c, "OP_1SUB", op1Sub, true, "1 is subtracted from the input"),
    OP_2MUL((byte) 0x8d, "OP_2MUL", op2Mul, false, "The input is multiplied by 2. disabled"),
    OP_2DIV((byte) 0x8e, "OP_2DIV", op2Div, false, "The input is divided by 2. disabled"),

    OP_NEGATE((byte) 0x8f, "OP_NEGATE", opNegate, true, "The sign of the input is flipped"),
    OP_ABS((byte) 0x90, "OP_ABS", opAbs, true, "The input is made positive"),
    OP_NOT((byte) 0x91, "OP_NOT", opNot, true,
            "If the input is 0 or 1, it is flipped. Otherwise the output will be 0."),
    OP_0NOTEQUAL((byte) 0x92, "OP_0NOTEQUAL", op0NotEqual, true, "Returns 0 if the input is 0. 1 otherwise"),

    OP_ADD((byte) 0x93, "OP_ADD", opAdd, true, "a is added to b"),
    OP_SUB((byte) 0x94, "OP_SUB", opSub, true, "b is subtracted from a"),

    OP_MUL((byte) 0x95, "OP_MUL", opMul, false, "a is multiplied by b. disabled"),
    OP_DIV((byte) 0x96, "OP_DIV", opDiv, false, "a is divided by b. disabled"),
    OP_MOD((byte) 0x97, "OP_MOD", opMod, false, "Returns the remainder after dividing a by b. disabled"),
    OP_LSHIFT((byte) 0x98, "OP_LSHIFT", opLShift, false, "Shifts a left b bits, preserving sign. disabled"),
    OP_RSHIFT((byte) 0x99, "OP_RSHIFT", opRShift, false, "Shifts a right b bits, preserving sign. disabled"),

    OP_BOOLAND((byte) 0x9a, "OP_BOOLAND", opBoolAnd, true, "If both a and b are not 0, the output is 1. Otherwise 0."),
    OP_BOOLOR((byte) 0x9b, "OP_BOOLOR", opBoolOr, true, "If a or b is not 0, the output is 1. Otherwise 0."),
    OP_NUMEQUAL((byte) 0x9c, "OP_NUMEQUAL", opNumEqual, true, "Returns 1 if the numbers are equal, 0 otherwise"),
    OP_NUMEQUALVERIFY((byte) 0x9d, "OP_NUMEQUALVERIFY", opNumEqualVerify, true,
            "Same as OP_NUMEQUAL, but runs OP_VERIFY afterward"),
    OP_NUMNOTEQUAL((byte) 0x9e, "OP_NUMNOTEQUAL", opNumNotEqual, true,
            "Returns 1 if the numbers are not equal, 0 otherwise"),
    OP_LESSTHAN((byte) 0x9f, "OP_LESSTHAN", opLessThan, true, "Returns 1 if a is less than b, 0 otherwise"),
    OP_GREATERTHAN((byte) 0xa0, "OP_GREATERTHAN", opGreaterThan, true,
            "Returns 1 if a is greater than b, 0 otherwise"),
    OP_LESSTHANOREQUAL((byte) 0xa1, "OP_LESSTHANOREQUAL", opLessThanOrEqual, true,
            "Returns 1 if a is less than or equal to b, 0 otherwise"),
    OP_GREATERTHANOREQUAL((byte) 0xa2, "OP_GREATERTHANOREQUAL", opGreaterThanOrEqual, true,
            "Returns 1 if a is greater than or equal to b, 0 otherwise"),
    OP_MIN((byte) 0xa3, "OP_MIN", opMin, true, "Returns the smaller of a and b"),
    OP_MAX((byte) 0xa4, "OP_MAX", opMax, true, "Returns the larger of a and b"),
    OP_WITHIN((byte) 0xa5, "OP_WITHIN", opWithin, true,
            "Returns 1 if x is within the specified range (left-inclusive), 0 otherwise"),

    // crypto codes

    OP_RIPEMD160((byte) 0xa6, "OP_RIPEMD160", opRipemd160, true, "The input is hashed using RIPEMD-160"),
    OP_SHA1((byte) 0xa7, "OP_SHA1", opSha1, true, "The input is hashed using SHA-1"),
    OP_SHA256((byte) 0xa8, "OP_SHA256", opSha256, true, "The input is hashed once using SHA-256"),
    OP_HASH160((byte) 0xa9, "OP_HASH160", opHash160, true,
            "The input is hashed twice: first with SHA-256 and then with RIPEMD-160"),
    OP_HASH256((byte) 0xaa, "OP_HASH256", opHash256, true,
            "The input is hashed two times with SHA-256"),
    OP_CODESEPARATOR((byte) 0xab, "OP_CODESEPARATOR", opCodeSeparator, true,
            "All of the signature checking words will only match signatures to the data after the most recently-executed OP_CODESEPARATOR."),
    OP_CHECKSIG((byte) 0xac, "OP_CHECKSIG", opCheckSig, true,
            "The entire transaction's outputs, inputs, and script (from the most recently-executed OP_CODESEPARATOR to the end) are hashed. The signature used by OP_CHECKSIG must be a valid signature for this hash and public key. If it is, 1 is returned, 0 otherwise."),
    OP_CHECKSIGVERIFY((byte) 0xad, "OP_CHECKSIGVERIFY", opCheckSigVerify, true,
            "Same as OP_CHECKSIG, but OP_VERIFY is executed afterward"),
    OP_CHECKMULTISIG((byte) 0xae, "OP_CHECKMULTISIG", opCheckMultiSig, true,
            "Compares the first signature against each public key until it finds an ECDSA match. Starting with the subsequent public key, it compares the second signature against each remaining public key until it finds an ECDSA match. The process is repeated until all signatures have been checked or not enough public keys remain to produce a successful result. All signatures need to match a public key. Because public keys are not checked again if they fail any signature comparison, signatures must be placed in the scriptSig using the same order as their corresponding public keys were placed in the scriptPubKey or redeemScript. If all signatures are valid, 1 is returned, 0 otherwise. Due to a bug, one extra unused value is removed from the stack."),
    OP_CHECKMULTISIGVERIFY((byte) 0xaf, "OP_CHECKMULTISIGVERIFY", opCheckMultiSigVerify, true,
            "Same as OP_CHECKMULTISIG, but OP_VERIFY is executed afterward"),

    // expansion codes

    OP_NOP1((byte) 0xb0, "OP_NOP1", opNoOp, true, "The word is ignored. Does not mark transaction as invalid"),
    // Locktime
    OP_CHECKLOCKTIMEVERIFY((byte) 0xb1, "OP_CHECKLOCKTIMEVERIFY", opCheckLocktimeVerify, true, "Marks transaction as invalid if the top stack item is greater than the transaction's nLockTime field, otherwise script evaluation continues as though an OP_NOP was executed. Transaction is also invalid if 1. the stack is empty; or 2. the top stack item is negative; or 3. the top stack item is greater than or equal to 500000000 while the transaction's nLockTime field is less than 500000000, or vice versa; or 4. the input's nSequence field is equal to 0xffffffff. The precise semantics are described in BIP 0065."), // introduced in BIP 65, replacing OP_NOP2
    // OP_NOP2((byte) 0xb1, opNoOp, true, "The word is ignored. Does not mark transaction as invalid"),

    OP_CHECKSEQUENCEVERIFY((byte) 0xb2, "OP_CHECKSEQUENCEVERIFY", opCheckSequenceVerify, true, "Marks transaction as invalid if the relative lock time of the input (enforced by BIP 0068 with nSequence) is not equal to or longer than the value of the top stack item. The precise semantics are described in BIP 0112"),
    // OP_NOP3((byte) 0xb1, 0xb2, true, "The word is ignored. Does not mark transaction as invalid"),

    OP_NOP4((byte) 0xb3, "OP_NOP4", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),
    OP_NOP5((byte) 0xb4, "OP_NOP5", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),
    OP_NOP6((byte) 0xb5, "OP_NOP6", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),
    OP_NOP7((byte) 0xb6, "OP_NOP7", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),
    OP_NOP8((byte) 0xb7, "OP_NOP8", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),
    OP_NOP9((byte) 0xb8, "OP_NOP9", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),
    OP_NOP10((byte) 0xb9, "OP_NOP10", opNoOp, true, "The word is ignored. Does not mark transaction as invalid."),

    OP_INVALIDOPCODE((byte) MASK_0xFF, "OP_INVALIDOPCODE", opInvalidOpCode, true, "Matches any opcode that is not yet assigned"),


    // Miscellaneous, newer codes not yet understood by me

    /*
    OP_INT_0x02((byte) 0x02, "OP_INT_0x02 (?)", opInvalidOpCode, true,
            "TODO No description found in https://en.bitcoin.it/wiki/Script"),

    OP_SEQUENCE_0x30((byte) 0x30, "OP_SEQUENCE_0x30 (?)", opInvalidOpCode, true,
            "TODO No description found in https://en.bitcoin.it/wiki/Script"),
    OP_LENGTH_0x21((byte) 0x21, "OP_LENGTH_0x21 (?)", opInvalidOpCode, true,
            "TODO No description found in https://en.bitcoin.it/wiki/Script"),
    OP_LENGTH_0x46((byte) 0x46, "OP_LENGTH_0x46 (?)", opInvalidOpCode, true,
            "TODO No description found in https://en.bitcoin.it/wiki/Script"),

    OP_DATA_0x21((byte) 0x21, "OP_DATA_0x21 (?)", opInvalidOpCode, true,
            "TODO No description found in https://en.bitcoin.it/wiki/Script"),
    OP_DATA_0x49((byte) 0x49, "OP_DATA_0x49 (?)", opInvalidOpCode, true,
            "TODO No description found in https://en.bitcoin.it/wiki/Script"),
     */

    // Pseudo-words used internally for assisting with transaction matching;  invalid if used in actual scripts.
    OP_PUBKEYHASH((byte) 0xfd, "OP_PUBKEYHASH (?)", opInvalidOpCode, true,
            "Represents a public key hashed with OP_HASH160"),
    OP_PUBKEY((byte) 0xfe, "OP_PUBKEY (?)", opInvalidOpCode, true,
            "Represents a public key compatible with OP_CHECKSIG");

    final byte code;
    final String asmname;
    final Object function;
    final boolean enabled;
    final String description;

    OpCode(byte code, String asmname, Object function, boolean enabled, String description) {
        this.code = code;
        this.asmname = asmname; // as defined in https://github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/script.cpp
        this.function = function;
        this.enabled = enabled;
        this.description = description;
    }

    public byte code() {
        return this.code;
    }

    public String asmname() {
        return this.asmname;
    }

    public Object function() {
        return this.function;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public String description() {
        return this.description;
    }

    @Override
    public String toString() {
        return "OpCode{" +
                "name=" + this.name() +
                "  code=" + code +
                "  asmname=" + asmname +
                "  enabled=" + enabled +
                ", description='" + description + '\'' +
                '}';
    }
}
