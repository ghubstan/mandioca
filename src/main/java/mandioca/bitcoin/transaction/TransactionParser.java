package mandioca.bitcoin.transaction;

import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.parser.DataInputStreamParser;
import mandioca.bitcoin.parser.Parser;
import mandioca.bitcoin.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkConstants.HASH_LENGTH;
import static mandioca.bitcoin.network.NetworkConstants.SEGWIT_MARKER;
import static mandioca.bitcoin.script.Script.StandardScripts.*;
import static mandioca.bitcoin.transaction.TxIn.SEQUENCE_0xFFFFFFFF;
import static mandioca.bitcoin.util.HexUtils.HEX;

class TransactionParser {

    private static final Logger log = LoggerFactory.getLogger(TransactionParser.class);

    private static final Function<TxIn[], Boolean> ignoreLocktime = (txIns) -> {
        // locktime is ignored if every tx input's sequence is 0xffffffff (-1)
        boolean[] ignoreLocktime = {true};
        Arrays.stream(txIns).forEach(i -> {
            if (!Arrays.equals(i.sequence, SEQUENCE_0xFFFFFFFF)) {
                ignoreLocktime[0] = false;
            }
        });
        return ignoreLocktime[0];
    };

    private Parser parser;
    private final Supplier<Integer> parseVersion = () -> bytesToInt.apply(reverse.apply(parser.readBytes(Integer.BYTES)));
    private final Supplier<byte[]> parseSegwitMarker = () -> parser.readBytes(2);
    private final Supplier<Integer> parseLocktime = () -> bytesToInt.apply(reverse.apply(parser.readBytes(Integer.BYTES)));

    public TransactionParser() {
    }

    public TransactionParser init(ByteArrayInputStream bais) {
        this.parser = new DataInputStreamParser(new DataInputStream(bais));
        return this;
    }

    public Tx parse(NetworkType networkType) {
        try {
            if (isSegwitTransaction()) {
                // isSegwitTransaction() is not foolproof (TODO fix)
                return parseSegwit(networkType);
            } else {
                return parsePreSegwit(networkType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading from input stream", e);
        } finally {
            parser.closeInputStream();
        }
    }

    public Tx parseSegwit(NetworkType networkType) {
        try {
            // Takes a byte stream and parses the transaction at the start return a Tx object
            int version = parseVersion.get();           // version is an integer in 4 bytes, little-endian
            byte[] segwitMarker = parseSegwitMarker.get();
            if (isSegwitMarker(segwitMarker)) {
                throw new RuntimeException("invalid segwit transaction;  5th and 6th bytes "
                        + Arrays.toString(segwitMarker) + "  != " + Arrays.toString(SEGWIT_MARKER));
            }
            TxIn[] txIns = parseInputs();
            TxOut[] txOuts = parseOutputs();

            // Parse segwit TxIn[]
            // TODO factor out into private method
            for (TxIn txIn : txIns) {
                if (parser.hasRemaining()) {
                    int numSegwitItems = (int) parser.readVarint();
                    byte[][] segwitItems = new byte[numSegwitItems][];
                    for (int i = 0; i < numSegwitItems; i++) {
                        int itemLength = (int) parser.readVarint();
                        segwitItems[i] = itemLength == 0 ? ZERO_BYTE : parser.readBytes(itemLength);
                    }
                    txIn.witness = segwitItems;
                } else {
                    throw new RuntimeException("error parsing segwit tx-inputs");
                }
            }

            int locktime = parseLocktime(txIns);        // locktime is an integer in 4 bytes, little-endian
            if (parser.hasRemaining()) {
                throw new RuntimeException("did not read all byte while parsing transaction");
            } else {
                return new Tx(version, txIns, txOuts, locktime, true, networkType);
            }
        } catch (Exception e) {
            throw new RuntimeException("error parsing segwit transaction", e);
        } finally {
            parser.closeInputStream();
        }
    }

    public Tx parsePreSegwit(NetworkType networkType) {
        try {
            // Takes a byte stream and parses the transaction at the start return a Tx object
            int version = parseVersion.get();           // version is an integer in 4 bytes, little-endian
            TxIn[] txIns = parseInputs();
            TxOut[] txOuts = parseOutputs();
            int locktime = parseLocktime(txIns);        // locktime is an integer in 4 bytes, little-endian
            if (parser.hasRemaining()) {
                throw new RuntimeException("did not read all byte while parsing transaction");
            } else {
                return new Tx(version, txIns, txOuts, locktime, networkType);
            }
        } catch (Exception e) {
            throw new RuntimeException("error parsing transaction", e);
        } finally {
            parser.closeInputStream();
        }
    }

    TxIn[] parseInputs() {
        long numInputs = parser.readVarint();
        List<TxIn> txInputList = new ArrayList<>();
        int txInIndex = 0;
        try {
            while (txInIndex < numInputs) {
                // Don't forget to reverse(appropriate fields) when caching parse results
                byte[] previousTransactionId = reverse.apply(parser.readBytes(HASH_LENGTH)); // little endian of hash256 of prev tx contents
                byte[] previousTransactionIndex = reverse.apply(parser.readBytes(Integer.BYTES));  // (4 bytes, little endian)
                Script script = Script.parse(parser.getInputStream()); // (variable length)
                byte[] sequence = reverse.apply(parser.readBytes(Integer.BYTES));  // (4 bytes, little endian)
                txInputList.add(new TxIn(previousTransactionId, previousTransactionIndex, script, sequence));
                txInIndex++;
            }
        } catch (Exception e) {
            throw new RuntimeException("error decoding TxIn[" + txInIndex + "]");
        }
        return txInputList.toArray(new TxIn[0]);
    }


    TxOut[] parseOutputs() {
        long numOutputs = parser.readVarint();
        List<TxOut> txOutputList = new ArrayList<>();
        int txOutIndex = 0;
        try {
            while (txOutIndex < numOutputs) {
                byte[] amount = reverse.apply(parser.readBytes(Long.BYTES)); // read satoshi amt serialized in next 8 bytes, little endian
                // log.info("parsed txOut.amount:  {}", toLong.apply(amount));
                long scriptPubKeyLength = parser.readVarint();
                byte[] scriptPubKeyBytes = parser.readBytes((int) scriptPubKeyLength);
                final Script script;

                if (isOpReturnTransactionBytes.apply(amount, scriptPubKeyBytes)) {
                    // TODO do I need to do this for TxIn parser method?
                    // log.info("TxOut [{}] is a null data transaction", txOutIndex);
                    script = parseNullDataTransactionPubKey.apply(scriptPubKeyBytes);

                } else if (isP2pkhScriptPubKeyBytes.test(scriptPubKeyBytes)) {

                    script = parseP2pkhPubKey.apply(scriptPubKeyBytes);

                } else if (isP2shScriptPubKeyBytes.test(scriptPubKeyBytes)) {

                    script = parseP2shPubKey.apply(scriptPubKeyBytes);

                } else if (isP2wpkhScriptPubKeyBytes.test(scriptPubKeyBytes)) {

                    // TODO what is this extra byte?  the hash length?  what?
                    //      It's the witness.length varint
                    //      See verifyInput(int inputIndex)
                    script = hashToP2wpkhScript.apply(subarray.apply(scriptPubKeyBytes, 2, scriptPubKeyBytes.length - 2));

                } else if (isP2wshScriptPubKeyBytes.test(scriptPubKeyBytes)) {

                    // TODO what is this extra byte?  the hash length?  what?
                    //      It's the witness.length varint
                    //      See verifyInput(int inputIndex)
                    script = hashToP2wshScript.apply(subarray.apply(scriptPubKeyBytes, 2, scriptPubKeyBytes.length - 2));

                } else {

                    throw new RuntimeException("error decoding TxOut[" + txOutIndex + "]"
                            + " with unknown or unsupported script for serialized script containing "
                            + scriptPubKeyLength + " bytes: " + HEX.encode(scriptPubKeyBytes));
                }
                TxOut txOut = new TxOut(amount, script);
                // log.info("decoded TxOut[{}]:  {}", txOutIndex, txOut);
                txOutputList.add(txOut);
                txOutIndex++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error decoding tx outputs", e);
        }
        return txOutputList.toArray(new TxOut[0]);
    }

    private int parseLocktime(TxIn[] txIns) {
        if (ignoreLocktime.apply(txIns)) {
            parser.readBytes(Integer.BYTES); // read and ignore 4 bytes to exhaust the stream
            return 0;
        }
        return parseLocktime.get();
    }

    private boolean isSegwitTransaction() {
        try {
            parser.getInputStream().skipBytes(4);
            boolean isSegwit = parser.read() == 0x00;
            parser.reset();
            return isSegwit;
        } catch (IOException e) {
            throw new RuntimeException("error checking 5th byte to determine if segwit tx", e);
        }
    }

    private boolean isSegwitMarker(byte[] bytes) {
        return !Arrays.equals(SEGWIT_MARKER, bytes);
    }
}
