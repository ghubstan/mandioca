package mandioca.bitcoin.script.processing;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

import static java.lang.System.out;
import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.script.processing.AbstractOpFunctions.ENC_1;
import static mandioca.bitcoin.script.processing.AbstractOpFunctions.isTrue;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.script.processing.OpCodeFunction.doOpForSignatureHash;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

public class CryptoOpFunctionsTest extends OpCodeFunctionsTest {

    private static final String input1 = "Listen. Strange women lying in ponds distributing swords is no basis for a "
            + "system of government. Supreme executive power derives from a mandate from the masses, not from "
            + "some farcical aquatic ceremony.";
    private static final String input2 = "Bally Jerry pranged his kite right in the how’s-your-father; hairy blighter, "
            + "dicky-birded, feathered back on his sammy, took a waspy, flipped over on his Betty Harpers and "
            + "caught his can in the Bertie.";
    private static final String input3 = "Kilimanjaro is a pretty tricky climb you know, most of its up until you reach "
            + "the very very top, and then it tends to slope away rather sharply.";
    private static final String input4 = "It’s passed on! This parrot is no more! It has ceased to be! "
            + "It’s expired and gone to meet its maker! This is a late parrot! It’s a stiff! "
            + "Bereft of life, it rests in peace! If you hadn’t nailed it to the perch, "
            + "it would be pushing up the daisies! It’s rung down the curtain and joined the choir invisible. "
            + "This is an ex-parrot!";
    private static final String input5 = "My philosophy, like color television, is all there in black and white.";
    private static final String input6 = "There's nothing an agnostic can't do if he doesn't know whether he believes in anything or not.";
    private static final String input7 = "A reading from the Book of Armaments, Chapter 4, Verses 16 to 20 Then did "
            + "he raise on high the Holy Hand Grenade of Antioch, saying, Bless this, O Lord, that with it thou mayst "
            + "blow thine enemies to tiny bits, in thy mercy. And the people did rejoice and did feast upon the lambs "
            + "and toads and tree-sloths and fruit-bats and orangutans and breakfast cereals Now did the Lord say, "
            + "First thou pullest the Holy Pin. Then thou must count to three. Three shall be the number of the "
            + "counting and the number of the counting shall be three. Four shalt thou not count, neither shalt thou "
            + "count two, excepting that thou then proceedeth to three. Five is right out. Once the number three, "
            + "being the number of the counting, be reached, then lobbest thou the Holy Hand Grenade in the direction "
            + "of thine foe, who, being naughty in my sight, shall snuff it.";

    // @BeforeClass  // Don't need to print s after hash tests created/passed
    public static void printInputs() {
        out.println("Hash inputs:");
        out.println("Input1 = " + input1);
        out.println("Hex(input1) = " + HEX.encode(stringToBytes.apply(input1)));
        out.println("Input2 = " + input2);
        out.println("Hex(input2) = " + HEX.encode(stringToBytes.apply(input2)));
        out.println("Input3 = " + input3);
        out.println("Hex(input3) = " + HEX.encode(stringToBytes.apply(input3)));
        out.println("Input4 = " + input4);
        out.println("Hex(input4) = " + HEX.encode(stringToBytes.apply(input4)));
        out.println("Input5 = " + input5);
        out.println("Hex(input5) = " + HEX.encode(stringToBytes.apply(input5)));
        out.println("Input6 = " + input6);
        out.println("Hex(input6) = " + HEX.encode(stringToBytes.apply(input6)));
        out.println("Input7 = " + input7);
        out.println("Hex(input7) = " + HEX.encode(stringToBytes.apply(input7)));
    }

    @Before
    public void setup() {
        clearStacks();
    }

    @Test
    public void testOpRipemd160() {
        // Expected RIPEMD160 hashes calculated at https://www.browserling.com/tools/ripemd160-hash
        assertHashResult(OP_RIPEMD160, input1, "8adbe91503b33a7feb6a84977c6aa2da71d49dd5");
        assertHashResult(OP_RIPEMD160, input2, "f4698bb1e7d478034a74cdf76aca268bd9c78041");
        assertHashResult(OP_RIPEMD160, input3, "8371f5fc7f0358f2ef79f951c130fffae8a5406d");
        assertHashResult(OP_RIPEMD160, input4, "191dc689d76523418b477be06b521925706a838a");
        assertHashResult(OP_RIPEMD160, input5, "9fedb41b685036bc9d2c06c6030e5c5fd695eaef");
        assertHashResult(OP_RIPEMD160, input6, "dcbdd07b28364113f63aafadf9fe6be29eb07288");
        assertHashResult(OP_RIPEMD160, input7, "62e8c538d7e71cd09f1b4a153acf27f312c2883e");
    }

    @Test
    public void testOpSha1() {
        //  Expected SHA1 hashes calculated at https://www.browserling.com/tools/sha1-hash
        assertHashResult(OP_SHA1, input1, "4fd346c562b7d2084386d04ad91c81c8f45b5ef9");
        assertHashResult(OP_SHA1, input2, "d51357f701cef5621e4d4063c37b2173b8085286");
        assertHashResult(OP_SHA1, input3, "54f79b399f5cdbb3b3a02706d14be1831afe8ee5");
        assertHashResult(OP_SHA1, input4, "b94a5c517fc6789267a49d7e0e32a12588b5af1c");
        assertHashResult(OP_SHA1, input5, "8fd5811b65b1f7827a2b74d4181557b3ec5bca77");
        assertHashResult(OP_SHA1, input6, "862fb4a9d40a4e1953c3c67f100c7c4a2d930281");
        assertHashResult(OP_SHA1, input7, "f3b022f0a7b2ce73ac4a61a6b0aebff8a27f1f56");
    }

    @Test
    public void testOpSha256() {
        //  Expected SHA-256 hashes calculated at https://www.browserling.com/tools/sha256-hash
        assertHashResult(OP_SHA256, input1, "f817f042bae7a2c38c4aa9b685f87bca2a0828549e8d6568db046bcbf0c48e20");
        assertHashResult(OP_SHA256, input2, "16ddf483376674bd06632709d44179ff4e31e9d2dcecb9277035f52b01e1c0b0");
        assertHashResult(OP_SHA256, input3, "6c856d1b3774a51b03d2bf53df37b4998d66b44d265e8a6c0948fb998e605264");
        assertHashResult(OP_SHA256, input4, "c7b8dacdcd46dbdb5299e72da17cf85fb5b32dd1b39bb6fd0e835c26456485b6");
        assertHashResult(OP_SHA256, input5, "c0bf6c9b674ef55fed9e645d987e2ca527cc8fbc4df0b35c4e3a5ef49094446d");
        assertHashResult(OP_SHA256, input6, "9f90cabe9c20e1983fbd797ca484075c2b525258253aa71d5f7fcde633532977");
        assertHashResult(OP_SHA256, input7, "817b1a4a253cbc5a4996410dcaecd9024553b5d6cbfe131ae8384801192e6bb9");
    }

    @Test
    public void testOpHash256() { // dbl sha256 hash
        // Expected double sha256 hashes calculated at // https://2coin.org/doublesha256.html
        // (Printed hex of each input and plug into the web form.)
        assertHashResult(OP_HASH256, input1, "19c5ca65ef0a19a46fd00ba149bcbc04bfee9e9a69e791619dd9201a36cad1fc");
        assertHashResult(OP_HASH256, input2, "3166129407ee007856feab07799ce10ab83d693d9f4f6502ff12fb34c8b0e173");
        assertHashResult(OP_HASH256, input3, "6bf627877d35afe2fb351bd148a9353a4abbc02bee6f4c3104c2383dde72a363");
        assertHashResult(OP_HASH256, input4, "d105283870a62ca17f9db63193388ef18ff589595901616c616c9bcbd36e2e38");
        assertHashResult(OP_HASH256, input5, "304a91b798b6ff23b152656189aac15f538bbcd60b17e8d09b811c05c137e48c");
        assertHashResult(OP_HASH256, input6, "42c82516d4de406b45e205c5bea588b9334312f1f9a95e3c25372e0f60143d53");
        assertHashResult(OP_HASH256, input7, "678bf3c2b51dcd2ea75bb27f35fb37a916c066b09807803fb38f6ae3d8e1c33e");
    }

    @Test
    public void testOpHash160() {
        // Chapter 6, Exercise 1
        assertHashResult(OP_HASH160, "hello world", "d7d5ee7824ff93f94c3055af9382c86c68b5ca92");

        //  See https://en.bitcoin.it/wiki/Technical_background_of_version_1_Bitcoin_addresses
        //  https://bitcoinvalued.com/tools.php
        // TODO So far, I have no way of verifying my hash160 function is working, due BTC address specific info on the web.
        //  I'm getting ahead of myself.  I may have to wait until I write address generating tests and use interim results here.
        // not valid tests
        //noinspection ConstantConditions
        if (false) {
            assertHashResult(OP_HASH160, input1, "742481933c5ab056f4848ccfc0291d01c2d028e1");
            assertHashResult(OP_HASH160, input2, "02b44a5f2f74046f6ecfdaf2eadf84f910aed119");
            assertHashResult(OP_HASH160, input3, "e14fcc746c78e93e0346e6d506209bae32a0fed5");
            assertHashResult(OP_HASH160, input4, "20faf740c1f3a71038befda969ba4ccab9eebe67");
            assertHashResult(OP_HASH160, input5, "ff0c812b859fb7b0e0ea6dae20369f2d14729ba2");
            assertHashResult(OP_HASH160, input6, "52e35e727224e053affd1a2efcf2cfe874539170");
            assertHashResult(OP_HASH160, input7, "bfd8e69e33fbfa16fca3e12cee35e07ff4491825");
        }
    }


    @Ignore
    @Test
    public void testOpCodeSeparator() {  // TODO
        // All of the signature checking words will only match signatures
        // to the data after the most recently-executed OP_CODESEPARATOR.
        doOp(OP_1, true);
        doOp(OP_CODESEPARATOR, true);
    }

    @Test
    public void testOpChecksig() {
        // The entire transaction's outputs, inputs, and script (from the most recently-executed OP_CODESEPARATOR
        // to the end) are hashed. The signature used by OP_CHECKSIG must be a valid signature for this hash and
        // public key. If it is, 1 is returned, 0 otherwise.
        //
        // See https://en.bitcoin.it/wiki/OP_CHECKSIG

        // Chapter 6, Exercise 2
        BigInteger z = new BigInteger("7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d", HEX_RADIX);
        byte[] sec = HEX.decode("04887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e4da568744d06c61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34");
        byte[] der = HEX.decode("3045022000eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c022100c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab601");
        stack().push(der);
        stack().push(sec);
        assertTrue(doOpForSignatureHash(OP_CHECKSIG, z));
        assertPopMatchesElement(ENC_1);
        assertEquals(0, stack().size());
    }

    @Test
    public void testOpChecksigVerify() {
        // Same as OP_CHECKSIG, but OP_VERIFY is executed afterward.
        // See https://en.bitcoin.it/wiki/OP_CHECKSIG

        // Chapter 6, Exercise 2
        BigInteger z = new BigInteger("7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d", HEX_RADIX);
        byte[] sec = HEX.decode("04887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e4da568744d06c61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34");
        byte[] der = HEX.decode("3045022000eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c022100c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab601");
        stack().push(der);
        stack().push(sec);
        assertTrue(doOpForSignatureHash(OP_CHECKSIGVERIFY, z));
        assertEquals(0, stack().size());
    }

    @Test
    public void testOpCheckMultisig() {
        // Compares the first signature against each public key until it finds an ECDSA match. Starting with the
        // subsequent public key, it compares the second signature against each remaining public key until it finds
        // an ECDSA match. The process is repeated until all signatures have been checked or not enough public keys
        // remain to produce a successful result. All signatures need to match a public key. Because public keys are
        // not checked again if they fail any signature comparison, signatures must be placed in the scriptSig using
        // the same order as their corresponding public keys were placed in the scriptPubKey or redeemScript. If all
        // signatures are valid, 1 is returned, 0 otherwise. Due to OP_CHECKMULTISIG's off-by-one bug, one extra unused
        // value is removed from the stack.
        //
        // See https://en.bitcoin.it/wiki/OP_CHECKSIG

        // Chapter 8, OpTest def test_op_checkmultisig
        //
        // def test_op_checkmultisig(self):
        //        z = 0xe71bfa115715d6fd33796948126f40a8cdd39f187e4afb03896795189fe1423c
        //        sig1 = bytes.fromhex('3045022100dc92655fe37036f47756db8102e0d7d5e28b3beb83a8fef4f5dc0559bddfb94e02205a36d4e4e6c7fcd16658c50783e00c341609977aed3ad00937bf4ee942a8993701')
        //        sig2 = bytes.fromhex('3045022100da6bee3c93766232079a01639d07fa869598749729ae323eab8eef53577d611b02207bef15429dcadce2121ea07f233115c6f09034c0be68db99980b9a6c5e75402201')
        //        sec1 = bytes.fromhex('022626e955ea6ea6d98850c994f9107b036b1334f18ca8830bfff1295d21cfdb70')
        //        sec2 = bytes.fromhex('03b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb71')
        //        stack = [b'', sig1, sig2, b'\x02', sec1, sec2, b'\x02']
        //        self.assertTrue(op_checkmultisig(stack, z))
        //        self.assertEqual(decode_num(stack[0]), 1)
        //

        // OpCodeFunction.enableStackDebug();
        BigInteger z = new BigInteger("e71bfa115715d6fd33796948126f40a8cdd39f187e4afb03896795189fe1423c", HEX_RADIX);
        byte[] sig1 = HEX.decode("3045022100dc92655fe37036f47756db8102e0d7d5e28b3beb83a8fef4f5dc0559bddfb94e02205a36d4e4e6c7fcd16658c50783e00c341609977aed3ad00937bf4ee942a8993701");
        byte[] sig2 = HEX.decode("3045022100da6bee3c93766232079a01639d07fa869598749729ae323eab8eef53577d611b02207bef15429dcadce2121ea07f233115c6f09034c0be68db99980b9a6c5e75402201");
        byte[] sec1 = HEX.decode("022626e955ea6ea6d98850c994f9107b036b1334f18ca8830bfff1295d21cfdb70");
        byte[] sec2 = HEX.decode("03b287eaf122eea69030a0e9feed096bed8045c8b98bec453e1ffac7fbdbd4bb71");

        // create stack = (tail) [b'', sig1, sig2, b'\x02', sec1, sec2, b'\x02'] (head)
        doOp(OP_0, true);
        stack().push(sig1);
        stack().push(sig2);
        doOp(OP_2, true);
        stack().push(sec1);
        stack().push(sec2);
        doOp(OP_2, true);

        assertTrue(doOpForSignatureHash(OP_CHECKMULTISIG, z));
        assertTrue(isTrue.apply(stack().pop()));
        // OpCodeFunction.disableStackDebug();
    }


    @Ignore
    @Test
    public void testOpCheckMultisigVerify() {  // TODO
        // Same as OP_CHECKMULTISIG, but OP_VERIFY is executed afterward.
        //
        // See https://en.bitcoin.it/wiki/OP_CHECKSIG
        doOp(OP_1, true);
        doOp(OP_CHECKMULTISIGVERIFY, true);
    }


    private void assertHashResult(OpCode opCode, String input, String expectedHex) {
        stack().push(stringToBytes.apply(input));
        doOp(opCode, true);
        assertEquals(1, stack().size());
        assertArrayEquals(HEX.decode(expectedHex), stack().pop());
    }
}


