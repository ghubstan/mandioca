package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.script.Interpreter;
import mandioca.bitcoin.script.Script;
import mandioca.bitcoin.script.ScriptError;
import org.junit.Test;

import java.math.BigInteger;

import static java.math.BigInteger.ZERO;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertTrue;
import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class ScriptEvaluationTest extends OpCodeFunctionsTest {

    @Test   // From J Song Book "Programming Bitcoin", Chapter 6, Example 1
    public void testChapter6Example1EvalP2PKScript() {
        BigInteger z = new BigInteger("7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d", HEX_RADIX);
        byte[] sec = HEX.decode("04887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e4da568744d06c61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34");
        byte[] der = HEX.decode("3045022000eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c022100c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab601");
        Script scriptPubKey = new Script(createCommandSet(sec, new byte[]{OP_CHECKSIG.code})); // 2 cmds: SEC, OP_CHECKSIG
        Script scriptSig = new Script(new byte[][]{der}); // 1 cmd
        Script script = scriptSig.add(scriptPubKey); // scriptSig is always op top
        // Script.dumpCommands(script, "Chapter 6, Example 1 ScriptSig + ScriptPubKey:");
        Interpreter interpreter = new Interpreter(script, z, new ScriptError(), true);
        assertTrue(interpreter.evaluateScript());
        ScriptError scriptError = interpreter.getScriptError();
        // TODO set & check correct error code
    }


    @Test // Demonstrates Chapter 6, Figures 25, 26
    public void testArbitraryScript() {
        Script scriptPubKey = new Script(createCommandSet(
                OP_5.code,
                OP_ADD.code,
                OP_9.code,
                OP_EQUAL.code));
        Script scriptSig = new Script(new byte[][]{new byte[]{OP_4.code}});
        Script script = scriptSig.add(scriptPubKey);
        // Script.dumpCommands(script, "Arbitrary Script:");
        Interpreter interpreter = new Interpreter(script, ZERO, new ScriptError(), true);
        assertTrue(interpreter.evaluateScript());
    }


    @Test   // From J Song Book "Programming Bitcoin", Chapter 6, Exercise 3
    public void testChapter6Exercise3EvalP2PKScript() {
        // Changed example pubkey setup to NOT use disabled OP_MUL, and OP_ADD instead, as explained below.
        Script scriptPubKey = new Script(
                createCommandSet(
                        (byte) 0x76,
                        (byte) 0x76,
                        // (byte) 0x95,  // OP_MUL is disabled, use add(2+2) instead and get same result
                        (byte) 0x93,
                        (byte) 0x93,
                        (byte) 0x56,
                        (byte) 0x87));
        byte[] sigBytes = new byte[]{(byte) 0x52};
        Script scriptSig = new Script(new byte[][]{sigBytes});
        Script script = scriptSig.add(scriptPubKey); // scriptSig always on top
        // Script.dumpCommands(script, "Chapter 6, Exercise 3 ScriptSig + ScriptPubKey:");
        Interpreter interpreter = new Interpreter(script, ZERO, new ScriptError(), true);
        assertTrue(interpreter.evaluateScript());
        ScriptError scriptError = interpreter.getScriptError();
        // TODO set & check correct error code
    }

    @Test   // From J Song Book "Programming Bitcoin", Chapter 6, Exercise 4
    public void testChapter6Exercise4BreakSHA1Pinata() {
        // https://cryptos.com/the-consequences-of-the-broken-sha1-algorithm/
        // In September 2013, a cryptography researcher, Peter Todd, announced a bounty on discovering a SHA1 collision,
        // in other words, breaking this particular hash function. The challenge involved breaking the code script that
        // Todd created by submitting two messages different in value, but with the same hash digest value.
        // This would grant access to the bounty address, making the solver able to move the 2.5 bitcoin bounty from
        // one address to another.
        //
        // On 23rd of February, Google/CWI team’s declared that they were able to create a hash collision through a
        // 10,000 times more efficient method than just guessing each possible value.
        // Figure out what this script script pubkey is doing:  6e 87 91 69 a7 7c a7 87
        Script scriptPubKey = new Script(
                createCommandSet(
                        (byte) 0x6e,
                        (byte) 0x87,
                        (byte) 0x91,
                        (byte) 0x69,
                        (byte) 0xa7,
                        (byte) 0x7c,
                        (byte) 0xa7,
                        (byte) 0x87));
        String collision1Hex = "255044462d312e330a25e2e3cfd30a0a0a312030206f626a0a3c3c2f576964746820" +
                "32203020522f4865696768742033203020522f547970652034203020522f537562747970652035" +
                "203020522f46696c7465722036203020522f436f6c6f7253706163652037203020522f4c656e67" +
                "74682038203020522f42697473506572436f6d706f6e656e7420383e3e0a73747265616d0affd8" +
                "fffe00245348412d3120697320646561642121212121852fec092339759c39b1a1c63c4c97e1ff" +
                "fe017f46dc93a6b67e013b029aaa1db2560b45ca67d688c7f84b8c4c791fe02b3df614f86db169" +
                "0901c56b45c1530afedfb76038e972722fe7ad728f0e4904e046c230570fe9d41398abe12ef5bc" +
                "942be33542a4802d98b5d70f2a332ec37fac3514e74ddc0f2cc1a874cd0c78305a215664613097" +
                "89606bd0bf3f98cda8044629a1";
        String collision2Hex = "255044462d312e330a25e2e3cfd30a0a0a312030206f626a0a3c3c2f576964746820" +
                "32203020522f4865696768742033203020522f547970652034203020522f537562747970652035" +
                "203020522f46696c7465722036203020522f436f6c6f7253706163652037203020522f4c656e67" +
                "74682038203020522f42697473506572436f6d706f6e656e7420383e3e0a73747265616d0affd8" +
                "fffe00245348412d3120697320646561642121212121852fec092339759c39b1a1c63c4c97e1ff" +
                "fe017346dc9166b67e118f029ab621b2560ff9ca67cca8c7f85ba84c79030c2b3de218f86db3a9" +
                "0901d5df45c14f26fedfb3dc38e96ac22fe7bd728f0e45bce046d23c570feb141398bb552ef5a0" +
                "a82be331fea48037b8b5d71f0e332edf93ac3500eb4ddc0decc1a864790c782c76215660dd3097" +
                "91d06bd0af3f98cda4bc4629b1";
        assertNotSame(collision1Hex, collision2Hex);

        byte[] collision1 = HEX.decode(collision1Hex);
        byte[] collision2 = HEX.decode(collision2Hex);
        byte[][] sigScriptCommands = new byte[][]{collision1, collision2};
        Script scriptSig = new Script(sigScriptCommands);
        Script script = scriptSig.add(scriptPubKey); // scriptSig must be 1st (op top)
        // Script.dumpCommands(script, "Chapter 6, Exercise 4 scriptSig + scriptPubKey:");

        Interpreter interpreter = new Interpreter(script, ZERO, new ScriptError(), true);
        assertTrue(interpreter.evaluateScript());
        ScriptError scriptError = interpreter.getScriptError();
        // TODO set & check correct error code
    }


    // TODO dedup
    private byte[][] createCommandSet(final byte[]... commands) {
        byte[][] cmds = new byte[commands.length][];
        System.arraycopy(commands, 0, cmds, 0, commands.length);
        return cmds;
    }

    // TODO dedup
    private byte[][] createCommandSet(final byte... commands) {
        byte[][] cmds = new byte[commands.length][];
        for (int i = 0; i < commands.length; i++) {
            cmds[i] = new byte[]{commands[i]};
        }
        return cmds;
    }
}
