package mandioca.bitcoin.script.processing;

/**
 * Script error codes as defined in https://github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/script_error.h
 */
public enum ScriptErrorCode {

    SCRIPT_ERR_OK("No error"),
    SCRIPT_ERR_UNKNOWN_ERROR("unknown error"),
    SCRIPT_ERR_EVAL_FALSE("Script evaluated without error but finished with a false/empty top stack element"),
    SCRIPT_ERR_OP_RETURN("OP_RETURN was encountered"),

    /* Max sizes */
    SCRIPT_ERR_SCRIPT_SIZE("Script is too big"),
    SCRIPT_ERR_PUSH_SIZE("Push value size limit exceeded"),
    SCRIPT_ERR_OP_COUNT("Operation limit exceeded"),
    SCRIPT_ERR_STACK_SIZE("Stack size limit exceeded"),
    SCRIPT_ERR_SIG_COUNT("Signature count negative or greater than pubkey count"),
    SCRIPT_ERR_PUBKEY_COUNT("Pubkey count negative or limit exceeded"),

    /* Failed verify operations */
    SCRIPT_ERR_VERIFY("Script failed an OP_VERIFY operation"),
    SCRIPT_ERR_EQUALVERIFY("Script failed an OP_EQUALVERIFY operation"),
    SCRIPT_ERR_CHECKMULTISIGVERIFY("Script failed an OP_CHECKMULTISIGVERIFY operation"),
    SCRIPT_ERR_CHECKSIGVERIFY("Script failed an OP_CHECKSIGVERIFY operation"),
    SCRIPT_ERR_NUMEQUALVERIFY("Script failed an OP_NUMEQUALVERIFY operation"),

    /* Logical/Format/Canonical errors */
    SCRIPT_ERR_BAD_OPCODE("Opcode missing or not understood"),
    SCRIPT_ERR_DISABLED_OPCODE("Attempted to use a disabled opcode"),
    SCRIPT_ERR_INVALID_STACK_OPERATION("Operation not valid with the current stack size"),
    SCRIPT_ERR_INVALID_ALTSTACK_OPERATION("Operation not valid with the current altstack size"),
    SCRIPT_ERR_UNBALANCED_CONDITIONAL("Invalid OP_IF construction"),

    /* CHECKLOCKTIMEVERIFY and CHECKSEQUENCEVERIFY */
    SCRIPT_ERR_NEGATIVE_LOCKTIME("Negative locktime"),
    SCRIPT_ERR_UNSATISFIED_LOCKTIME("Locktime requirement not satisfied"),

    /* Malleability */
    SCRIPT_ERR_SIG_HASHTYPE("Signature hash type missing or not understood"),
    SCRIPT_ERR_SIG_DER("Non-canonical DER signature"),
    SCRIPT_ERR_MINIMALDATA("Data push larger than necessary"),
    SCRIPT_ERR_SIG_PUSHONLY("Only non-push operators allowed in signatures"),
    SCRIPT_ERR_SIG_HIGH_S("Non-canonical signature: S value is unnecessarily high"),
    SCRIPT_ERR_SIG_NULLDUMMY("Dummy CHECKMULTISIG argument must be zero"),
    SCRIPT_ERR_PUBKEYTYPE("Public key is neither compressed or uncompressed"),
    SCRIPT_ERR_CLEANSTACK("Extra items left on stack after execution"),
    SCRIPT_ERR_MINIMALIF("OP_IF/NOTIF argument must be minimal"),
    SCRIPT_ERR_SIG_NULLFAIL("Signature must be zero for failed CHECK(MULTI)SIG operation"),

    /* softfork safeness */
    SCRIPT_ERR_DISCOURAGE_UPGRADABLE_NOPS("NOPx reserved for soft-fork upgrades"),
    SCRIPT_ERR_DISCOURAGE_UPGRADABLE_WITNESS_PROGRAM("Witness version reserved for soft-fork upgrades"),

    /* segregated witness */
    SCRIPT_ERR_WITNESS_PROGRAM_WRONG_LENGTH("Witness program has incorrect length"),
    SCRIPT_ERR_WITNESS_PROGRAM_WITNESS_EMPTY("Witness program was passed an empty witness"),
    SCRIPT_ERR_WITNESS_PROGRAM_MISMATCH("Witness program hash mismatch"),
    SCRIPT_ERR_WITNESS_MALLEATED("Witness requires empty scriptSig"),
    SCRIPT_ERR_WITNESS_MALLEATED_P2SH("Witness requires only-redeemscript scriptSig"),
    SCRIPT_ERR_WITNESS_UNEXPECTED("Witness provided for non-witness script"),
    SCRIPT_ERR_WITNESS_PUBKEYTYPE("Using non-compressed keys in segwit"),

    /* Constant scriptCode */
    SCRIPT_ERR_OP_CODESEPARATOR("Using OP_CODESEPARATOR in non-witness script"),
    SCRIPT_ERR_SIG_FINDANDDELETE("Signature is found in scriptCode"),

    SCRIPT_ERR_ERROR_COUNT("unknown error");

    final String description;

    ScriptErrorCode(String description) {
        this.description = description;
    }

    public String description() {
        return this.description;
    }
}
