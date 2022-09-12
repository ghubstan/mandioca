package mandioca.bitcoin.script;

import mandioca.bitcoin.script.processing.Op;
import mandioca.bitcoin.script.processing.OpCode;
import mandioca.bitcoin.script.processing.ScriptStack;
import mandioca.bitcoin.stack.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Predicate;

import static java.lang.System.arraycopy;
import static java.lang.System.err;
import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.HashFunctions.sha256Hash;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;
import static mandioca.bitcoin.script.Script.StandardScripts.hashToP2pkhScript;
import static mandioca.bitcoin.script.processing.Op.getOpCode;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.script.processing.OpCodeFunction.*;
import static mandioca.bitcoin.script.processing.ScriptConstants.MAX_STACK_SIZE;
import static mandioca.bitcoin.script.processing.ScriptConstants.POSITIVE_ZERO;
import static mandioca.bitcoin.script.processing.ScriptErrorCode.*;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

/**
 * Script interpreter based on bitcoin core src https://github.com/bitcoin/bitcoin/blob/v0.18.1/src/script/interpreter.cpp
 */
public final class Interpreter {

    private static final Logger log = LoggerFactory.getLogger(Interpreter.class);

    private static final String BAD_INFO_MESSAGE = "Bad op:  %s";

    private final Predicate<Stack> isBip0016Stack = (s) -> {
        byte[][] cmds = s.toArray(new byte[0][]);
        return cmds.length == 3
                && cmds[0].length == 1 && Op.isOpCode.apply(cmds[0][0], OP_HASH160)
                && cmds[1].length == 20
                && cmds[2].length == 1 && Op.isOpCode.apply(cmds[2][0], OP_EQUAL);
    };

    private final Predicate<Stack> isSegwitV0ForP2wpkhMainStack = (s) ->
            s.size() == 2 && s.peek(0).length == 0 && s.peek(1).length == 20;

    private final Predicate<Stack> isSegwitV0ForP2wshMainStack = (s) ->
            s.size() == 2 && s.peek(0).length == 0 && s.peek(1).length == 32;

    private final Script script;
    private final BigInteger signatureHash; // 'z'
    private final byte[][] witness;
    private final ScriptError scriptError;
    private boolean debugStack;

    public Interpreter(Script script, BigInteger signatureHash, byte[][] witness, ScriptError scriptError) {
        this.script = script;
        this.signatureHash = signatureHash;
        this.witness = witness;
        this.scriptError = scriptError;
    }

    public Interpreter(Script script, BigInteger signatureHash, byte[][] witness, ScriptError scriptError, boolean debugStack) {
        this(script, signatureHash, witness, scriptError);
        this.debugStack = debugStack;
    }

    public Interpreter(Script script, BigInteger signatureHash, ScriptError scriptError, boolean debugStack) {
        this(script, signatureHash, new byte[][]{}, scriptError);
        this.debugStack = debugStack;
    }


    // See https://raw.githubusercontent.com/bitcoin/bitcoin/v0.18.1/src/script/interpreter.cpp for reference impl
    public boolean evaluateScript() {
        if (debugStack) {
            log.info("evaluate script {}", script.asm());
        }
        clearStacks();
        final Stack cmdStack = createCommandStack(); // cmds change during eval, make a copy stack
        if (debugStack) {
            log.info("executing script cmd stack ops:");
        }
        while (!cmdStack.empty()) {
            byte[] cmd = cmdStack.pop();
            if (cmd.length == 1) {
                OpCode op = getOpCode.apply(cmd[0]);   // should be OP_DUP, OP_CHECKSIG, ... assert?
                if (!executeStackOp(op)) {
                    return false;
                }
            } else {
                mainStack().push(cmd);      // add the cmd to the stack
                if (isBip0016Stack.test(cmdStack)) {
                    if (!executeBip0016P2shRule(cmd, cmdStack)) {
                        return false;
                    }
                }
                if (isSegwitV0ForP2wpkhMainStack.test(mainStack())) {
                    executeSegwitV0ForP2wpkRule(cmdStack);
                }
                if (isSegwitV0ForP2wshMainStack.test(mainStack())) {
                    if (!executeSegwitV0ForP2wshRule(cmdStack)) {
                        return false;
                    }
                }
            }
        }
        if (debugStack) {
            cmdStack.dumpStack("Final cmd stack:  " + (cmdStack.empty() ? "empty" : ""));
            mainStack().dumpStack("Final main stack:");
        }
        return evaluationWasSuccessful();
    }

    public ScriptError getScriptError() {
        return scriptError;
    }

    private boolean evaluationWasSuccessful() {
        if (mainStack().empty()) {
            scriptError.setScriptErrorCode(SCRIPT_ERR_UNKNOWN_ERROR);
            return false;   // fail the script if the main stack is empty
        } else if (Arrays.equals(mainStack().pop(), POSITIVE_ZERO)) {
            scriptError.setScriptErrorCode(SCRIPT_ERR_EVAL_FALSE);
            return false;   // fail the script if shutdown top is empty array  (what about checking for negative zero?)
        } else {
            scriptError.setScriptErrorCode(SCRIPT_ERR_OK);
            return true;    // any other result means script has been validated
        }
        // TODO check validity by checking stack size and peeking for isTrue or isFalse
    }

    private boolean executeStackOp(OpCode op) {
        if (op.equals(OP_IF) || op.equals(OP_NOTIF)) {                          // cmd in (99, 100)
            // TODO these two ops request manipulation of cmds array based on top element of stack
            // TODO implement OP_IF, OP_NOTIF (takes cmdStack arg)
            //noinspection ConstantConditions
            if (false) {
                scriptError.setScriptErrorCode(SCRIPT_ERR_UNKNOWN_ERROR);
                err.printf(BAD_INFO_MESSAGE, op.asmname());
                return false;
            }
        } else if (op.equals(OP_TOALTSTACK) || op.equals(OP_FROMALTSTACK)) {    // cmd in (107, 108)
            if (!doOp(op)) {
                scriptError.setScriptErrorCode(SCRIPT_ERR_UNKNOWN_ERROR);
                err.printf(BAD_INFO_MESSAGE, op.asmname());
                return false;
            }
        } else if (op.equals(OP_CHECKSIG) || op.equals(OP_CHECKSIGVERIFY) // cmd in (172, 173, 174, 175)
                || op.equals(OP_CHECKMULTISIG) || op.equals(OP_CHECKMULTISIGVERIFY)) {
            if (!doOpForSignatureHash(op, signatureHash)) {
                scriptError.setScriptErrorCode(SCRIPT_ERR_UNKNOWN_ERROR);
                err.printf(BAD_INFO_MESSAGE, op.asmname());
                return false;
            }
        } else {
            if (!doOp(op)) {
                scriptError.setScriptErrorCode(SCRIPT_ERR_UNKNOWN_ERROR);
                err.printf(BAD_INFO_MESSAGE, op.asmname());
                return false;
            }
        }
        return true; // no error
    }

    private boolean executeBip0016P2shRule(byte[] cmd, Stack cmdStack) {
        // execute p2sh rule
        cmdStack.pop();  // ignore OP_HASH160
        byte[] redeemScriptHash160 = cmdStack.pop();
        cmdStack.pop();  // ignore OP_EQUAL
        if (!doOp(OP_HASH160)) { // On mainStack
            return false;
        }
        mainStack().push(redeemScriptHash160);
        if (!doOp(OP_EQUAL)) { // On mainStack
            return false;
        }
        if (!doOp(OP_VERIFY)) { // On mainStack
            log.error("bad p2sh redeem script hash 160");
            return false;
        }
        byte[] redeemScriptLength = VARINT.encode(cmd.length);
        byte[] rawRedeemScript = concatenate.apply(redeemScriptLength, cmd);
        Script redeemScript = Script.parse(rawRedeemScript);
        for (int i = redeemScript.getCmds().length - 1; i >= 0; i--) {
            cmdStack.push(redeemScript.getCmds()[i]);
        }
        return true;
    }

    private void executeSegwitV0ForP2wpkRule(Stack cmdStack) {
        // execute witness program version 0;  make a p2pkh combined script
        // from the 20-byte hash, signature, and pubkey before evaluation
        byte[] hash160 = mainStack().pop();
        // the 2nd element is the witness version '0'
        mainStack().pop();
        for (byte[] witnessElement : witness) {
            cmdStack.putLast(witnessElement);
        }
        Script script = hashToP2pkhScript.apply(hash160);
        for (byte[] scriptCmd : script.getCmds()) {
            cmdStack.putLast(scriptCmd);
        }
        // cmdStack.dumpStack("\ncmd-stack after segwit v0 p2wpkh extensions");
        // log.info();
    }

    private boolean executeSegwitV0ForP2wshRule(Stack cmdStack) {
        // execute witness program version 0;  make a p2sh combined script
        // the top element is the sha256 hash of of the witness script (not double hash 256)
        byte[] sha256 = mainStack().pop();
        // the 2nd element is the witness version '0'
        mainStack().pop();
        for (int i = 0; i <= witness.length - 2; i++) {
            // everything but the witness script is added to the cmd stack
            cmdStack.putLast(witness[i]);
        }
        // the witness script is the last element of the witness field
        byte[] witnessScript = witness[witness.length - 1];
        // the witness script must hash to the sha256 that was on the main stack
        if (!Arrays.equals(sha256, sha256Hash.apply(witnessScript))) {
            log.error("bad sha256 {} vs {}", HEX.encode(sha256), HEX.encode(witnessScript));
            return false;
        }
        // parse the witness script and add it to the cmd stack
        Script script = parseWitnessScript(witnessScript);
        for (byte[] scriptCmd : script.getCmds()) {
            cmdStack.putLast(scriptCmd);
        }
        // cmdStack.dumpStack("\ncmd-stack after p2wsh extensions");
        // log.info();
        return true;
    }

    private Script parseWitnessScript(byte[] witnessScript) {
        ByteBuffer byteBuffer = null;
        try {
            byte[] witnessScriptVarint = VARINT.encode(witnessScript.length);
            byteBuffer = borrowBuffer.apply(witnessScriptVarint.length + witnessScript.length);
            byteBuffer.put(witnessScriptVarint).put(witnessScript).flip();
            return Script.parse(byteBuffer.array());
        } finally {
            returnBuffer.accept(byteBuffer);
        }
    }

    private Stack createCommandStack() {
        // cmds change during eval, make a copy cmd stack
        byte[][] cmds = new byte[script.getCmds().length][];
        arraycopy(script.getCmds(), 0, cmds, 0, script.getCmds().length);
        final Stack cmdStack = debugStack
                ? new ScriptStack(MAX_STACK_SIZE, true, "cmd-stack  ")
                : new ScriptStack(MAX_STACK_SIZE);
        if (debugStack) {
            log.info("Initializing cmd stack with capacity {}", MAX_STACK_SIZE);
        }
        for (int i = cmds.length - 1; i >= 0; i--) {
            cmdStack.push(cmds[i]);
        }
        if (debugStack) {
            cmdStack.dumpStack("Initialized cmd stack:");
        }
        return cmdStack;
    }
}
