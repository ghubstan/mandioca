package mandioca.bitcoin.script;

import mandioca.bitcoin.address.Address;
import mandioca.bitcoin.function.ThrowingBiFunction;
import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.script.processing.OpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static mandioca.bitcoin.address.AddressFactory.*;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.ByteCompareFunctions.isEqual;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;
import static mandioca.bitcoin.script.Script.StandardScripts.*;
import static mandioca.bitcoin.script.processing.Op.getOpCode;
import static mandioca.bitcoin.script.processing.Op.isOpCode;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.script.processing.SigHashType.toSighashTypeAsmToken;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;


/**
 * A <b>Script</b> represents a command set that requires evaluation by combining the ScriptSig and ScriptPubKey
 * fields.
 * <p>
 * The TxIn contains the serialized ScriptSig, TxOut contains the serialized ScriptPubKey. The lockbox (ScriptPubKey)
 * and the unlocking mechanism (ScriptSig) are in different transactions. The lockbox is where BTC are received, the
 * unlocking script is where BTC are spent. The input of the spending tx points to the receiving tx. Since the ScriptSig
 * unlocks a ScriptPubKey, we combine the two scripts.  The ScriptSig commands go on top of the ScriptPubKey commands,
 * and command instructions are processed one at a time until completion for failure.
 * <p>
 * <b>Standard Scripts</b>
 * <ul>
 *      <li>p2pk (Pay-to-pubkey) ScriptPubKey stores public key in compressed or uncompressed SEC format.</li>
 *      <li>p2pkh (Pay-to-pubkey-hash) ScriptPubKey contains a 20 byte hash160(compressed SEC).</li>
 *      <li>p2sh (Pay-to-script-hash) ScriptPubKey stores public key TODO</li>
 *      <li>p2wpkh (Pay-to-witness-pubkey-hash) ScriptPubKey stores public key TODO</li>
 *      <li>p2wsh (Pay-to-witness-script-hash) ScriptPubKey stores public key TODO</li>
 * </ul>
 * <p>
 * Addresses  are known script templates such as those listed above.  Wallets know how to interpret various address types
 * (p2pkh, p2sh, p2wpkh) and create appropriate ScriptPubKeys.  All of the above examples have an address format of
 * either Base58 or Bech32 so wallets can pay to them.
 */
public class Script {

    private static final Logger log = LoggerFactory.getLogger(Script.class);

    // SEE http://localhost:8888/edit/code-ch13/script.py

    // assuming  scripts won't be parsed or serialized in parallel
    private static final ScriptParser parser = new ScriptParser();
    private static final ScriptSerializer serializer = new ScriptSerializer();

    private final byte[][] cmds;

    private final Supplier<Integer> scriptSigLength = () -> {
        int[] scriptSigLength = {0};
        Stream.of(this.getCmds()).forEach(c -> scriptSigLength[0] += c.length);
        return scriptSigLength[0];
    };
    ByteBuffer byteBuffer = ByteBuffer.allocate(0);

    public Script(byte[][] cmds) {
        this.cmds = cmds;
    }

    // Parse from complete script pub key, where the locking script is specified.
    public static Script parse(byte[] scriptPubKey) {
        return parser.init(scriptPubKey).parse();
    }

    // Parse from script pub key stream.
    public static Script parse(final DataInputStream is) {
        return parser.init(is).parse();
    }

    public Script add(Script other) {
        byte[][] combinedCommands = new byte[this.cmds.length + other.cmds.length][];
        arraycopy(this.cmds, 0, combinedCommands, 0, this.cmds.length);
        arraycopy(other.cmds, 0, combinedCommands, this.cmds.length, other.cmds.length);
        return new Script(combinedCommands);
    }

    /**
     * Returns the byte serialization of the Script.
     *
     * @return byte[]
     */
    public byte[] serialize() {
        return serializer.init(this).serialize();
    }

    public byte[][] getCmds() {
        return cmds;
    }

    public byte[] getScriptSig() {  // the unlocking script, proving ownership, which authorizes spending
        // Each cmd is prefixed with varint(cmd.len)
        byteBuffer = ByteBuffer.allocate(cmds.length + scriptSigLength.get());
        for (byte[] c : cmds) {
            byte[] cmdLength = VARINT.encode(c.length);
            byteBuffer.put(cmdLength);
            byteBuffer.put(c);
        }
        return byteBuffer.array();
    }

    @Deprecated // TODO de-confuse me
    public String scriptSigAsm() {
        if (isScriptSigOfP2pkh.test(cmds)) {
            StringBuilder asmBuilder = new StringBuilder();

            byte[] der = cmds[0];
            byte[] derAsmBytes = subarray.apply(der, 0, der.length - 1);
            asmBuilder.append(HEX.encode(derAsmBytes));
            asmBuilder.append(toSighashTypeAsmToken.apply(der[der.length - 1]));

            asmBuilder.append(HEX.encode(cmds[1])); // append pubkey
            return asmBuilder.toString();

        } else if (isScriptSigOfP2SomethingElse.test(cmds)) {

            // log.warn("isScriptSigOfP2SomethingElse  [TODO]");

        } else {

            log.error("UNKNOWN ScriptSig type  [TODO]");
            // throw new RuntimeException("UNKNOWN ScriptSig type");
        }
        return asm();
    }

    /**
     * Returns this Script's asm (assembly), the symbolic representation of the Bitcoin's Script language op-codes.
     *
     * @return String
     */
    public String asm() {
        // TODO refactor these script type checks into method
        if (isP2pkhScriptPubKey.test(this)) {
            // System.out.println(P2PKH.name());
        } else if (isP2shScriptPubKey.test(this)) {
            // System.err.println(P2SH.name());
        } else {
            if (isScriptSigOfP2pkh.test(cmds)) {
                //    err.println("Looks like a 2 cmd script sig");
            } else {
                // TODO Could be a script in mid evaluation
                //  System.err.println("TODO Could be a script in mid evaluation");
            }
        }

        // generic asm builder
        StringBuilder asmBuilder = new StringBuilder();
        Stream.of(cmds).forEachOrdered(c -> {
            if (c.length == 1) {
                asmBuilder.append(getOpCode.apply(c[0]).asmname());
            } else {
                asmBuilder.append(HEX.encode(c));
            }
            asmBuilder.append(" ");
        });
        if (asmBuilder.length() > 0) {
            asmBuilder.deleteCharAt(asmBuilder.length() - 1);
        }
        return asmBuilder.toString();
    }

    public Address address(NetworkType networkType) {
        if (isOpReturnTransactionPubKey.test(this)) {
            // parseNullDataTransactionPubKey.apply()
            byte[] arbitraryData = this.getCmds()[1];
            String arbitraryMessage = new String(arbitraryData, UTF_8).trim();
            log.warn("creating null data address for null data tx script {} with arbitrary data {} decoded {}",
                    this, HEX.encode(arbitraryData), arbitraryMessage);
            return nullDataAddress.apply(networkType);
        } else if (isP2pkhScriptPubKey.test(this)) {
            return scriptHashToP2pkh.apply(cmds[2], networkType);
        } else if (isP2shScriptPubKey.test(this)) {
            return scriptHashToP2sh.apply(cmds[1], networkType);
        } else {
            throw new RuntimeException("unknown ScriptPubKey cmds[0]=" + HEX.encode(this.cmds[0]) + "  cmds[1]=" + HEX.encode(this.cmds[1]));
        }
    }

    @Override
    public String toString() {
        String asm = asm();
        return "Script{" + (asm.length() == 0 ? "" : asm) + "}";
    }

    public static class StandardScripts {

        public static final Predicate<byte[][]> isScriptSigOfP2pkh = (cmds) -> cmds.length == 2 && cmds[0].length == 71 && cmds[1].length == 33;

        public static final Predicate<byte[][]> isScriptSigOfP2SomethingElse = (cmds) -> true;

        // The purpose of pay-to-script-hash is to move the responsibility for supplying the
        // conditions to redeem a transaction from the sender of the funds to the redeemer.
        public static final Predicate<Script> isP2pkhScriptPubKey = (script) -> {
            //  5 cmds:  OP_DUP (0x76), OP_HASH160 (0xa9), 20-byte hash, OP_EQUALVERIFY (0x88), OP_CHECKSIG (0xac)
            byte[][] cmds = script.getCmds();
            return cmds.length == 5
                    && isOpCode.apply(cmds[0][0], OP_DUP)
                    && isOpCode.apply(cmds[1][0], OP_HASH160)
                    && cmds[2].length == 20
                    && isOpCode.apply(cmds[3][0], OP_EQUALVERIFY)
                    && isOpCode.apply(cmds[4][0], OP_CHECKSIG);
        };

        public static final Predicate<Script> isP2wpkhScriptPubKey = (script) -> {
            // def is_p2wpkh_script_pubkey(self):  # <2>
            //        return len(self.cmds) == 2 and self.cmds[0] == 0x00 \
            //            and type(self.cmds[1]) == bytes and len(self.cmds[1]) == 20
            //    length=22, then  2 cmds:  OP_0 (0x00), 20-byte hash
            byte[][] cmds = script.getCmds();
            return cmds.length == 2
                    && cmds[0].length == 1 && isOpCode.apply(cmds[0][0], OP_0)
                    && cmds[1].length == 20; // 20 byte hash
        };

        public static final Predicate<byte[]> isP2pkhScriptPubKeyBytes = (bytes) -> {
            //  PubKey length=25, then 5 cmds:  OP_DUP (0x76), OP_HASH160 (0xa9), 20-byte hash, OP_EQUALVERIFY (0x88), OP_CHECKSIG (0xac)
            return bytes.length == 25
                    && isOpCode.apply(bytes[0], OP_DUP)
                    && isOpCode.apply(bytes[1], OP_HASH160)
                    // bytes[2 - 22] is hash160
                    && isOpCode.apply(bytes[23], OP_EQUALVERIFY)
                    && isOpCode.apply(bytes[24], OP_CHECKSIG);
        };

        public static final Predicate<Script> isP2shScriptPubKey = (script) -> {
            byte[][] cmds = script.getCmds();
            // 3 cmds:  OP_HASH160 (0xa9), 20-byte hash, OP_EQUAL (0x87)
            return cmds.length == 3
                    && isOpCode.apply(cmds[0][0], OP_HASH160)
                    && cmds[1].length == 20
                    && isOpCode.apply(cmds[2][0], OP_EQUAL);
        };


        public static final Predicate<Script> isP2wshScriptPubKey = (script) -> {
            //  def is_p2wsh_script_pubkey(self):
            //        return len(self.cmds) == 2 and self.cmds[0] == 0x00 \
            //            and type(self.cmds[1]) == bytes and len(self.cmds[1]) == 32
            byte[][] cmds = script.getCmds();
            return cmds.length == 2
                    && cmds[0].length == 1 && isOpCode.apply(cmds[0][0], OP_0)
                    && cmds[1].length == HASH_LENGTH;
        };

        public static final Predicate<byte[]> isP2shScriptPubKeyBytes = (bytes) -> {
            //  PubKey length=23, then  3 cmds:  OP_HASH160 (0xa9), 20-byte hash, OP_EQUAL (0x87)
            return bytes.length == 23
                    && isOpCode.apply(bytes[0], OP_HASH160)
                    && isOpCode.apply(bytes[22], OP_EQUAL);
        };

        // TODO this contradicts the book, which expects 21 bytes
        //      Or maybe it's my but, incorrectly calling parser.reset() all the way back to byte 0?
        public static final Predicate<byte[]> isP2wpkhScriptPubKeyBytes = (bytes) ->
                bytes.length == 22 && isOpCode.apply(bytes[0], OP_0)
                        && isEqual.apply(bytes[1], (byte) 0x014);   // 0x14 = 20

        // TODO this contradicts the book, which expects 33 bytes
        //      Or maybe it's my but, incorrectly calling parser.reset() all the way back to byte 0?
        public static final Predicate<byte[]> isP2wshScriptPubKeyBytes = (bytes) ->
                bytes.length == 34 && isOpCode.apply(bytes[0], OP_0)
                        && isEqual.apply(bytes[1], (byte) 0x20); // 0x20 = 32


        public static final Function<byte[], Script> hashToP2shScript = (hash160) -> {
            byte[][] cmds = createScriptCommands(
                    new byte[]{OP_HASH160.code()},
                    hash160,
                    new byte[]{OP_EQUAL.code()});
            return new Script(cmds);
        };

        public static final Function<byte[], Script> hashToP2pkhScript = (hash160) -> {
            byte[][] cmds = createScriptCommands(
                    new byte[]{OP_DUP.code()},
                    new byte[]{OP_HASH160.code()},
                    hash160,
                    new byte[]{OP_EQUALVERIFY.code()},
                    new byte[]{OP_CHECKSIG.code()});
            return new Script(cmds);
        };

        public static final Function<byte[], Script> hashToP2wpkhScript = (hash160) -> {
            // Takes a hash160 and returns the p2wpkh ScriptPubKey
            // def p2wpkh_script(h160):
            //    return Script([0x00, h160])  # <1>
            byte[][] cmds = createScriptCommands(new byte[]{OP_0.code()}, hash160);
            return new Script(cmds);
        };

        public static final Function<byte[], Script> hashToP2wshScript = (sha256) -> {
            // Takes a hash160 and returns the p2wsh ScriptPubKey
            // def p2wsh_script(h256):
            //    return Script([0x00, h256])  # <1>
            byte[][] cmds = createScriptCommands(new byte[]{0x00}, sha256);
            return new Script(cmds);
        };

        public static final Function<String, Script> addressToP2pkhScript = (a) -> {
            byte[] hash = legacyAddressToHash.apply(a);
            return hashToP2pkhScript.apply(hash);
        };

        public static final Predicate<Script> isOpReturnTransactionPubKey = (script) -> {
            byte[][] cmds = script.getCmds();
            // 2 cmds:  OP_RETURN (0x6a), some bytes
            return cmds.length == 2 && isOpCode.apply(cmds[0][0], OP_RETURN);
        };


        public static final BiFunction<byte[], byte[], Boolean> isOpReturnTransactionBytes = (amount, script) -> {
            // Null Data Transaction Type
            // SEE https://bitcoin.org/en/glossary/null-data-transaction
            // Definition
            //  A transaction type relayed and mined by default in Bitcoin Core 0.9.0 and later that adds arbitrary
            //  data to a provably unspendable pubkey script that full nodes don’t have to store in their UTXO database.
            //
            // Synonyms
            //  Null data transaction
            //  OP_RETURN transaction
            //  Data carrier transaction
            //
            // Not To Be Confused With OP_RETURN (an opcode used in one of the outputs in an OP_RETURN transaction)
            //
            // SEE https://bitcoin.org/en/transactions-guide#null-data
            //
            // Null data transaction type relayed and mined by default in Bitcoin Core 0.9.0 and later that adds arbitrary
            // data to a provably unspendable pubkey script that full nodes don’t have to store in their UTXO database.
            // It is preferable to use null data transactions over transactions that bloat the UTXO database because they
            // cannot be automatically pruned; however, it is usually even more preferable to store data outside transactions
            // if possible.
            //
            // Consensus rules allow null data outputs up to the maximum allowed pubkey script size of 10,000 bytes provided they
            // follow all other consensus rules, such as not having any data pushes larger than 520 bytes.
            //
            // Bitcoin Core 0.9.x to 0.10.x will, by default, relay and mine null data transactions with up to 40 bytes in a
            // single data push and only one null data output that pays exactly 0 satoshis:
            //
            // Pubkey Script: OP_RETURN <0 to 40 bytes of data>
            // (Null data scripts cannot be spent, so there's no signature script.)
            // Bitcoin Core 0.11.x increases this default to 80 bytes, with the other rules remaining the same.
            //
            // Bitcoin Core 0.12.0 defaults to relaying and mining null data outputs with up to 83 bytes with any number of data
            // pushes, provided the total byte limit is not exceeded. There must still only be a single null data output and it
            // must still pay exactly 0 satoshis.
            //
            // The -datacarriersize Bitcoin Core configuration option allows you to set the maximum number of bytes
            // in null data outputs that you will relay or mine.
            //
            return bytesToLong.apply(amount) == 0 && script.length > 0 && isOpCode.apply(script[0], OP_RETURN);
        };


        private final static ThrowingBiFunction<Byte, OpCode, byte[]> verifyAndCreateCommand = (b, op) -> {
            if (isEqual.apply(b, op.code())) {
                return new byte[]{op.code()};
            } else {
                throw new IllegalStateException("PubKey byte [" + HEX.toPositiveHex.apply(b) + "] is not "
                        + op.asmname() + " " + HEX.byteToPrefixedHex.apply(op.code()));
            }
        };


        /**
         * Parses a raw p2pkh pubkey and returns a script.
         */
        public static final Function<byte[], Script> parseP2pkhPubKey = (bytes) -> {
            // TODO where should this live?
            List<byte[]> cmdsList = new ArrayList<>();
            int count = 0;

            cmdsList.add(verifyAndCreateCommand.apply(bytes[count], OP_DUP));
            count += 1;
            cmdsList.add(verifyAndCreateCommand.apply(bytes[count], OP_HASH160));
            count += 1;

            int pubKeyLength = byteToInt.apply(bytes[count]); //  a varint that must be 20 for a valid hash160s
            if (pubKeyLength != 20) {
                throw new IllegalStateException("3rd PubKey byte is not 20, the valid length for a " + OP_HASH160.asmname());
            }
            count += 1;

            byte[] pubKey = new byte[pubKeyLength];
            arraycopy(bytes, count, pubKey, 0, pubKeyLength);
            cmdsList.add(pubKey);
            count += pubKeyLength;

            cmdsList.add(verifyAndCreateCommand.apply(bytes[count], OP_EQUALVERIFY));
            count += 1;
            cmdsList.add(verifyAndCreateCommand.apply(bytes[count], OP_CHECKSIG));
            count += 1;

            if (count != bytes.length) {
                throw new IllegalStateException("Input array too large for p2pkh PubKey");
            }

            byte[][] cmds = cmdsList.toArray(new byte[0][]);
            return new Script(cmds);
        };
        /**
         * Parses a raw p2sh pubkey and returns a script.
         */
        public static final Function<byte[], Script> parseP2shPubKey = (bytes) -> {
            // TODO where should this live?
            List<byte[]> cmdsList = new ArrayList<>();
            int count = 0;

            cmdsList.add(verifyAndCreateCommand.apply(bytes[count], OP_HASH160));
            count += 1;

            int pubKeyLength = byteToInt.apply(bytes[count]); //  a varint that must be 20 for a valid hash160s
            if (pubKeyLength != 20) {
                throw new IllegalStateException("3rd PubKey byte is not 20, the valid length for a " + OP_HASH160.asmname());
            }
            count += 1;

            byte[] pubKey = new byte[pubKeyLength];
            arraycopy(bytes, count, pubKey, 0, pubKeyLength);
            cmdsList.add(pubKey);
            count += pubKeyLength;

            cmdsList.add(verifyAndCreateCommand.apply(bytes[count], OP_EQUAL));
            count += 1;

            if (count != bytes.length) {
                throw new IllegalStateException("Input array too large for p2sh PubKey");
            }

            byte[][] cmds = cmdsList.toArray(new byte[0][]);
            return new Script(cmds);
        };

        public static final Function<byte[], Script> parseNullDataTransactionPubKey = (bytes) -> {
            // TODO where should this live?
            // 1st byte is the opcode OP_RETURN (6a);  2nd byte is the script length.
            if (!isOpCode.apply(bytes[0], OP_RETURN)) {
                throw new IllegalStateException("In a null data transaction script, the 1st byte in "
                        + HEX.encode(bytes) + " should be  the OP_RETURN code " + HEX.byteToHex.apply(OP_RETURN.code()));
            }
            // 1st byte is length, or could len be defined by more than 1 byte? (no where does it say I should be decoding a varint)
            int scriptLength = bytes[1];
            byte[] pubKeyBytes = subarray.apply(bytes, 2, scriptLength);
            return new Script(new byte[][]{new byte[]{OP_RETURN.code()}, pubKeyBytes});
        };

        static byte[][] createScriptCommands(final byte[]... commands) {
            // TODO make sure this is not defined elsewhere, use this only
            byte[][] cmds = new byte[commands.length][];
            arraycopy(commands, 0, cmds, 0, commands.length);
            return cmds;
        }
    }
}
