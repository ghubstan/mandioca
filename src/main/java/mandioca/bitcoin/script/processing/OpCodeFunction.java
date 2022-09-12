package mandioca.bitcoin.script.processing;


import mandioca.bitcoin.stack.Stack;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.script.processing.AbstractOpFunctions.decodeOpCodeN;
import static mandioca.bitcoin.script.processing.ScriptConstants.MAX_STACK_SIZE;

// See https://en.bitcoin.it/wiki/Script

public class OpCodeFunction {

    public static final byte[] ENC_0 = AbstractOpFunctions.ENC_0; // expose this to interpreter in pkg above (smelly)
    public static final byte[] ENC_1 = AbstractOpFunctions.ENC_1; // expose this to interpreter in pkg above (smelly)

    private final static Stack stack = new ScriptStack(MAX_STACK_SIZE);
    private final static Stack altStack = new ScriptStack(MAX_STACK_SIZE);

    private static final Consumer<DisabledOpCodeException> logDisabledOpCodeError = (e) -> {
        System.err.println(e.getMessage());
        System.err.println("\tShort stack trace of this logged error:");
        for (StackTraceElement l : e.getStackTrace()) {
            if (l.getModuleName() != null && l.getModuleName().equals("java.base")) {
                return;
            }
            System.err.println("\t\t" + l);
        }
    };

    public static void clearStacks() {
        stack.clear();
        altStack.clear();
    }

    public static Stack mainStack() {
        return stack;
    }

    public static Stack altStack() {
        return altStack;
    }

    public static void enableStackDebug() {  // for use by tests
        ((ScriptStack) stack).setDebug(true);
        ((ScriptStack) stack).setStackName("main-stack ");
        ((ScriptStack) altStack).setDebug(true);
        ((ScriptStack) altStack).setStackName("alt-stack  ");
    }

    public static void disableStackDebug() {  // for use by tests
        ((ScriptStack) stack).setDebug(false);
        ((ScriptStack) stack).setStackName(null);
        ((ScriptStack) altStack).setDebug(false);
        ((ScriptStack) altStack).setStackName(null);
    }


    public static boolean doOpForSignatureHash(OpCode op, BigInteger signatureHash /*z*/) {
        switch (op) {
            case OP_CHECKSIG:
            case OP_CHECKSIGVERIFY:
            case OP_CHECKMULTISIG:
            case OP_CHECKMULTISIGVERIFY:
                //noinspection rawtypes,unchecked
                return (boolean) ((BiFunction) op.function).apply(stack, signatureHash);
            default:
                throw new IllegalStateException("Don't know what to do with " + op.name());
        }
    }

    public static boolean doOp(OpCode op) {
        if (!op.enabled) {
            String err = String.format("Disabled op code %s is about to be executed.  If %s's function\n"
                            + "\tdoes not throw a DisabledOpCodeException up to the caller,  there is a\n"
                            + "\tbug in the op code's function implementation.  Confirm this op code should\n"
                            + "\tbe disabled.  If it should be disabled, modify %s's function definition\n"
                            + "\tto -> throwingDisabledOp.apply(%s)",
                    op.name(),
                    op.name(),
                    op.name(),
                    op.name());
            DisabledOpCodeException ex = new DisabledOpCodeException(err);
            logDisabledOpCodeError.accept(ex);
        }

        // TODO refactor big switch to allow for function arguments beyond just the stack
        switch (op) {
            case OP_FALSE:
            case OP_0:
            case OP_1NEGATE:
            case OP_TRUE:
            case OP_IFDUP:
            case OP_DEPTH:
            case OP_DROP:
            case OP_2DROP:
            case OP_DUP:
            case OP_2DUP:
            case OP_3DUP:
            case OP_2OVER:
            case OP_NIP:
            case OP_OVER:
            case OP_PICK:
            case OP_ROLL:
            case OP_ROT:
            case OP_2ROT:
            case OP_SWAP:
            case OP_2SWAP:
            case OP_TUCK:
            case OP_SIZE:
            case OP_EQUAL:
            case OP_VERIFY:
            case OP_1ADD:
            case OP_1SUB:
            case OP_EQUALVERIFY:
            case OP_NEGATE:
            case OP_ABS:
            case OP_NOT:
            case OP_0NOTEQUAL:
            case OP_ADD:
            case OP_SUB:
            case OP_BOOLAND:
            case OP_BOOLOR:
            case OP_NUMEQUAL:
            case OP_NUMEQUALVERIFY:
            case OP_NUMNOTEQUAL:
            case OP_LESSTHAN:
            case OP_GREATERTHAN:
            case OP_LESSTHANOREQUAL:
            case OP_GREATERTHANOREQUAL:
            case OP_MIN:
            case OP_MAX:
            case OP_WITHIN:
            case OP_RIPEMD160:
            case OP_SHA1:
            case OP_SHA256:
            case OP_HASH160:
            case OP_HASH256:
                //
                // TODO
                //
            case OP_CODESEPARATOR:
            case OP_CHECKLOCKTIMEVERIFY:
            case OP_CHECKSEQUENCEVERIFY:
                //
            case OP_IF:
            case OP_NOTIF:
            case OP_ELSE:
            case OP_ENDIF:
                //
                //
                //
                //noinspection rawtypes,unchecked
                return (boolean) ((Function) op.function).apply(stack);
            case OP_1:
            case OP_2:
            case OP_3:
            case OP_4:
            case OP_5:
            case OP_6:
            case OP_7:
            case OP_8:
            case OP_9:
            case OP_10:
            case OP_11:
            case OP_12:
            case OP_13:
            case OP_14:
            case OP_15:
            case OP_16:
                //noinspection rawtypes,unchecked
                return (boolean) ((BiFunction) op.function).apply(stack, decodeOpCodeN(op));
            case OP_PUSHDATA1:
            case OP_PUSHDATA2:
            case OP_PUSHDATA4:
                //noinspection rawtypes,unchecked
                return (boolean) ((BiFunction) op.function).apply(stack, new byte[]{op.code});
            case OP_NOP:
            case OP_NOP1:
            case OP_NOP4:
            case OP_NOP5:
            case OP_NOP6:
            case OP_NOP7:
            case OP_NOP8:
            case OP_NOP9:
            case OP_NOP10:
            case OP_RETURN:
            case OP_CAT:
            case OP_SUBSTR:
            case OP_LEFT:
            case OP_RIGHT:
            case OP_INVERT:
            case OP_AND:
            case OP_OR:
            case OP_XOR:
            case OP_2MUL:
            case OP_2DIV:
            case OP_MUL:
            case OP_DIV:
            case OP_MOD:
            case OP_LSHIFT:
            case OP_RSHIFT:
            case OP_PUBKEYHASH:
            case OP_PUBKEY:
            case OP_INVALIDOPCODE:
            case OP_RESERVED:
            case OP_VER:
            case OP_VERIF:
            case OP_VERNOTIF:
            case OP_RESERVED1:
            case OP_RESERVED2:
                //noinspection rawtypes
                return (boolean) ((Supplier) op.function).get();
            case OP_TOALTSTACK:
            case OP_FROMALTSTACK:
                //noinspection rawtypes,unchecked
                return (boolean) ((BiFunction) op.function).apply(stack, altStack);
            default:
                throw new IllegalStateException("Don't know what to do with " + op.name());
        }
    }
}

