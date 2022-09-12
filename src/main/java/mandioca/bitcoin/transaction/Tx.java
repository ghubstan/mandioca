package mandioca.bitcoin.transaction;

import mandioca.bitcoin.ecc.Secp256k1PrivateKey;
import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.script.Interpreter;
import mandioca.bitcoin.script.Script;
import mandioca.bitcoin.script.ScriptError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.*;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.NetworkType.*;
import static mandioca.bitcoin.script.Script.StandardScripts.*;
import static mandioca.bitcoin.script.processing.SigHashType.SIGHASH_ALL;
import static mandioca.bitcoin.transaction.TransactionSerializer.*;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

/**
 * https://github.com/jimmysong/programmingbitcoin/issues/63
 * <p>
 * "By far, this chapter wiped me out. Here are my thoughts/experiences/troubles."
 * ...
 * ...
 */
public class Tx implements Transaction {

    private static final Logger log = LoggerFactory.getLogger(Tx.class);

    // currently assumes  transactions won't be parsed or serialized in parallel
    private static final TransactionParser parser = new TransactionParser();
    private static final TransactionSerializer serializer = new TransactionSerializer();

    private static final byte[] COINBASE_TX_ID = emptyArray.apply(32); // all zeros
    private static final byte[] COINBASE_PREV_TX_ID_0xFFFFFFFF = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};  // -1

    // serialized fields
    protected final byte[] version;
    protected byte[] txInputs; // Cannot be final because tx inputs have to be signed (and this value re-set)
    protected final byte[] txOutputs;
    protected byte[] locktime;

    protected boolean isSegwit;
    protected byte[] hashPrevouts = null;
    protected byte[] hashSequence = null;
    protected byte[] hashOutputs = null;

    protected final NetworkType networkType;

    // deserialized fields populated in the pkg private constructor, or lazily
    private int deserializedVersion;
    private TxIn[] deserializedInputs;
    private TxOut[] deserializedOutputs;
    private int deserializedLocktime;

    public final Supplier<Boolean> isLocktimeAUnixTimestamp = () -> this.getDeserializedLocktime() >= 500000000;
    public final Supplier<Boolean> isLocktimeABlockNumber = () -> this.getDeserializedLocktime() < 500000000;
    public final Supplier<Boolean> isLocktimeIgnored = () -> this.getDeserializedLocktime() == 0;

    /**
     * Primary constructor that takes pre-serialized values for its parameters, excepting the networkType enum argument.
     * <p>
     * Serializing a Tx object is simply writing each of these arguments to a byte stream in the appropriate order
     * because the fields are already serialized.
     *
     * @param version     4 byte, little endian array
     * @param txInputs    variable length byte array, little endian, prepended with field length as varint
     * @param txOutputs   variable length byte array, little endian, prepended with field length as varint
     * @param locktime    4 byte, little endian array
     * @param networkType enum representing network MAINNET || TESTNET || REGTEST
     */
    public Tx(byte[] version, byte[] txInputs, byte[] txOutputs, byte[] locktime, NetworkType networkType) {
        this.version = version;
        this.txInputs = txInputs;
        this.txOutputs = txOutputs;
        this.locktime = locktime;
        this.networkType = networkType;
    }

    /**
     * Secondary constructor that takes deserialized values for its parameters.
     *
     * @param deserializedVersion  big endian int representing transaction version
     * @param deserializedInputs   array of deserialized transaction input objects
     * @param deserializedOutputs  array of deserialized transaction output objects
     * @param deserializedLocktime big endian int representing transaction locktime
     * @param isSegwit             flag is true if this is a segwit transaction
     * @param networkType          enum representing network MAINNET || TESTNET || REGTEST
     */
    Tx(int deserializedVersion, TxIn[] deserializedInputs, TxOut[] deserializedOutputs, int deserializedLocktime, boolean isSegwit, NetworkType networkType) {
        this(serializeVersion.apply(deserializedVersion),
                serializeTransactionInputs(deserializedInputs),
                serializeTransactionOutputs(deserializedOutputs),
                serializeLocktime.apply(deserializedLocktime),
                networkType);
        this.deserializedVersion = deserializedVersion;
        this.deserializedInputs = deserializedInputs;
        this.deserializedOutputs = deserializedOutputs;
        this.deserializedLocktime = deserializedLocktime;
        this.isSegwit = isSegwit;
    }

    Tx(int deserializedVersion, TxIn[] deserializedInputs, TxOut[] deserializedOutputs, int deserializedLocktime, NetworkType networkType) {
        this(deserializedVersion, deserializedInputs, deserializedOutputs, deserializedLocktime, false, networkType);
    }


    public String id() {
        return HEX.encode(hash());
    }

    public byte[] hash() {
        byte[] preSegwitBytes = serializer.init(this).serializePreSegwit();
        return reverse.apply(hash256.apply(preSegwitBytes));
    }

    public static Tx parse(ByteArrayInputStream bais, NetworkType networkType) {
        return parser.init(bais).parse(networkType);
    }

    public byte[] serialize() {
        return serializer.init(this).serialize();
    }

    /**
     * Resets the txInputs field with given signed deserialized tx inputs.
     * It changes nothing if called before signing deserialized tx inputs.
     */
    public void serializeSignedTxInputs() {
        this.txInputs = serializeTransactionInputs(getDeserializedInputs());
    }

    public long fee() {
        // Returns this transaction's fee in satoshis.
        long inputSum = 0, outputSum = 0;
        for (TxIn txIn : getDeserializedInputs()) {
            inputSum += txIn.valueAsLong(networkType);
        }
        for (TxOut txOut : getDeserializedOutputs()) {
            outputSum += txOut.getAmountAsLong();
        }
        return inputSum - outputSum;
    }

    public BigInteger sigHash(int inputIndex) {
        return sigHash(inputIndex, Optional.empty());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public BigInteger sigHash(int inputIndex, Optional<Script> redeemScript) {
        // Returns integer representation of the hash to be signed for TxIn[input_index].
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(version);                                        // start serialization with version
            baos.write(VARINT.encode(getDeserializedInputs().length));   // serializeInternal # inputs

            Script scriptSig;
            for (int i = 0; i < getDeserializedInputs().length; i++) {  // for each input
                TxIn txIn = getDeserializedInputs()[i];
                if (i == inputIndex) {  //  if input index is the one being signed...
                    // prev tx ScriptPubkey is ScriptSig
                    scriptSig = redeemScript.orElseGet(() -> txIn.scriptPubKey(networkType));
                } else {
                    scriptSig = new Script(new byte[][]{});      // else, ScriptSig is empty
                }

                // add serialization of input with the ScriptSig we want
                TxIn t = new TxIn(txIn.previousTransactionId, txIn.previousTransactionIndex, scriptSig, txIn.sequence);
                baos.write(t.serialize());
            }

            baos.write(VARINT.encode(getDeserializedOutputs().length));  // add serialized # outputs
            for (TxOut o : getDeserializedOutputs()) {                  // add serialized outputs
                baos.write(o.serialize());
            }

            baos.write(locktime);                                       // add locktime
            baos.write(SIGHASH_ALL.littleEndian());                     // add 4 byte littleEndian hash type SIGHASH_ALL
            byte[] hash = hash256.apply(baos.toByteArray());            // hash256 the serialization
            return new BigInteger(1, hash);                     // convert result to big endian integer
        } catch (IOException e) {
            throw new RuntimeException("Error computing signature hash z", e);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    BigInteger sigHashBip143(int inputIndex, Optional<Script> redeemScript, Optional<Script> witnessScript) {
        // fixes quadratic hashing problem?  (todo verify that's really true)

        log.info("********** sigHashBip143 **********");
        // Returns the integer representation of the hash that needs to get signed for index input_index
        //
        // See https://en.bitcoin.it/wiki/BIP_0143
        //  "Transaction Signature Verification for Version 0 Witness Program"
        //  Defines a new transaction digest algorithm for signature verification in version 0 witness program,
        //  in order to minimize redundant data hashing in verification, and to cover the input value by the signature.
        //
        //  def sig_hash_bip143(self, input_index, redeem_script=None, witness_script=None):
        //      tx_in = self.tx_ins[input_index]
        TxIn txIn = getDeserializedInputs()[inputIndex];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // per BIP143 spec
            // s = int_to_little_endian(self.version, 4)
            baos.write(version);
            // s += self.hash_prevouts() + self.hash_sequence()
            baos.write(hashPreviousOuts());
            log.info("\thashPreviousOuts = {}", HEX.encode(hashPreviousOuts()));

            baos.write(hashSequence());
            log.info("\thashSequence = {}", HEX.encode(hashSequence()));

            // [::-1] -> all items in the array, reversed
            // s += tx_in.prev_tx[::-1] + int_to_little_endian(tx_in.prev_index, 4)
            // baos.write(txIn.previousTransactionId);
            // baos.write(txIn.previousTransactionIndex);
            baos.write(reverse.apply(txIn.previousTransactionId));  // Using already serialized fields like a good lad
            baos.write(reverse.apply(txIn.previousTransactionIndex));

            final byte[] scriptCode;
            if (witnessScript.isPresent()) {
                // script_code = witness_script.serialize()
                scriptCode = witnessScript.get().serialize();
            } else if (redeemScript.isPresent()) {
                //  script_code = p2pkh_script(redeem_script.cmds[1]).serialize()
                scriptCode = hashToP2pkhScript.apply(redeemScript.get().getCmds()[1]).serialize();
            } else {
                //  script_code = p2pkh_script(tx_in.script_pubkey(self.testnet).cmds[1]).serialize()
                scriptCode = hashToP2pkhScript.apply(txIn.scriptPubKey(NETWORK).getCmds()[1]).serialize();
            }
            log.info("\tscriptCode = {}", HEX.encode(scriptCode));
            // s += script_code
            baos.write(scriptCode);

            // s += int_to_little_endian(tx_in.value(), 8)
            log.info("\ttxIn.value = {}", HEX.encode(toLittleEndian.apply(txIn.valueAsLong(NETWORK), Long.BYTES)));
            baos.write(toLittleEndian.apply(txIn.valueAsLong(NETWORK), Long.BYTES));

            // s += int_to_little_endian(tx_in.sequence, 4)
            log.info("\ttxIn.sequence = {}", HEX.encode(txIn.sequence));
            baos.write(txIn.sequence);

            // s += self.hash_outputs()
            log.info("\thashOutputs = {}", HEX.encode(hashOutputs()));
            baos.write(hashOutputs());

            // s += int_to_little_endian(self.locktime, 4)
            log.info("\tlocktime = {}", HEX.encode(locktime));
            baos.write(this.locktime);

            // s += int_to_little_endian(SIGHASH_ALL, 4)
            baos.write(SIGHASH_ALL.littleEndian());

            byte[] hash = hash256.apply(baos.toByteArray());            // hash256 the serialization
            log.info("\thash = {}", HEX.encode(hash));

            BigInteger z = new BigInteger(1, hash); // convert result to big endian integer
            log.info("\tz = {}", z);
            return z;
        } catch (IOException e) {
            throw new RuntimeException("error computing Bip 0134 signature hash z", e);
        }
    }

    byte[] hashPreviousOuts() {
        if (this.hashPrevouts == null) {
            // TODO Use already serialized fields after testing; use a ByteBuffer too, since we know size  byte arrays.
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                for (TxIn txIn : getDeserializedInputs()) {
                    //                  tx_in.prev_tx[::-1]    # all items in the array, reversed
                    //  all_prevouts += tx_in.prev_tx[::-1] + int_to_little_endian(tx_in.prev_index, 4)
                    baos.write(reverse.apply(txIn.previousTransactionId));  // Using already serialized fields like a good lad
                    baos.write(reverse.apply(txIn.previousTransactionIndex));

                    //String prevIdxHex = HEX.encode(reverse.apply(txIn.previousTransactionIndex));
                    //String prevIdHex = HEX.encode(reverse.apply(txIn.previousTransactionId));
                    //log.info("\tprevId={}", prevIdHex);
                    //log.info("\tprevIdx={}", prevIdxHex);
                }

                this.hashPrevouts = hash256.apply(baos.toByteArray());
                //String hashPrevoutsHex = HEX.encode(hashPrevouts);
                //log.info("\thashPrevouts={}", hashPrevoutsHex);

                baos.reset(); // use stream for hash sequence now
                for (TxIn txIn : getDeserializedInputs()) {
                    //  all_sequence += int_to_little_endian(tx_in.sequence, 4)
                    // baos.write(txInt.sequence);
                    baos.write(reverse.apply(txIn.sequence));
                }
                this.hashSequence = hash256.apply(baos.toByteArray());
                //String hashSequenceHex = HEX.encode(hashSequence);
                //log.info("\thashSequence={}", hashSequenceHex);

            } catch (IOException e) {
                throw new RuntimeException("error computing Bip 0134 signature hash z", e);
            }
        }
        return this.hashPrevouts;
    }

    byte[] hashSequence() {
        if (this.hashSequence == null) {
            this.hashPreviousOuts();    // calculate this.hashPrevouts
        }
        return this.hashSequence;
    }

    byte[] hashOutputs() {
        if (this.hashOutputs == null) {
            // TODO Use already serialized fields after testing; use a ByteBuffer too, since we know size  byte arrays.
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                for (TxOut txOut : getDeserializedOutputs()) {
                    baos.write(txOut.serialize());
                }
                this.hashOutputs = hash256.apply(baos.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("error computing Bip 0134 signature hash z", e);
            }
        }
        return this.hashOutputs;
    }


    private boolean verifyInput(int inputIndex) {
        TxIn txIn = getDeserializedInputs()[inputIndex];
        Script previousScriptPubKey = txIn.scriptPubKey(networkType);       //  grab previous ScriptPubKey
        BigInteger z;
        byte[][] witness;
        final Optional<Script> redeemScript;
        Optional<Script> witnessScript;
        // TODO fix Optional usage and decompose
        if (isP2shScriptPubKey.test(previousScriptPubKey)) {

            int lastCmdIdx = txIn.scriptSig.getCmds().length - 1;
            byte[] cmd = txIn.scriptSig.getCmds()[lastCmdIdx];
            byte[] redeemLength = VARINT.encode(cmd.length);
            byte[] rawRedeem = concatenate.apply(redeemLength, cmd);
            redeemScript = Optional.of(Script.parse(rawRedeem));

            // is the ScriptPubkey is a p2sh?
            if (isP2wpkhScriptPubKey.test(redeemScript.get())) {
                // handle  p2sh-p2wpkh case; redeem script could be p2wpkh or p2wsh
                z = sigHashBip143(inputIndex, redeemScript, Optional.empty());
                witness = txIn.witness;
            } else if (isP2wshScriptPubKey.test(redeemScript.get())) {
                // generate bip 0143 sig hash 'z'
                byte[] lastWitnessCmd = txIn.witness[txIn.witness.length - 1];
                byte[] rawWitness = concatenate.apply(VARINT.encode(lastWitnessCmd.length), lastWitnessCmd);
                witnessScript = Optional.of(Script.parse(rawWitness));
                z = sigHashBip143(inputIndex, Optional.empty(), witnessScript);
                witness = txIn.witness;
            } else {
                z = sigHash(inputIndex, redeemScript);
                witness = new byte[][]{};
            }

        } else {
            // handle p2wpkh case; script pub key could be p2wpkh or p2wsh
            if (isP2wpkhScriptPubKey.test(previousScriptPubKey)) {
                // generate bip 0143 sig hash 'z'
                z = sigHashBip143(inputIndex, Optional.empty(), Optional.empty());
                log.info("(want) 45391032779457205278777949011826389666341420727643090927362592749023688604144");
                witness = txIn.witness;

            } else if (isP2wshScriptPubKey.test(previousScriptPubKey)) {
                //  cmd = tx_in.witness[-1]   ... a[-1]    # last item in the array
                byte[] lastWitnessCmd = txIn.witness[txIn.witness.length - 1];
                //  raw_witness = encode_varint(len(cmd)) + cmd
                byte[] rawWitness = concatenate.apply(VARINT.encode(lastWitnessCmd.length), lastWitnessCmd);
                //  witness_script = Script.parse(BytesIO(raw_witness))
                witnessScript = Optional.of(Script.parse(rawWitness));
                // z = self.sig_hash_bip143(input_index, witness_script=witness_script)
                z = sigHashBip143(inputIndex, Optional.empty(), witnessScript);
                //  witness = tx_in.witness
                witness = txIn.witness;
            } else {
                z = sigHash(inputIndex);
                witness = new byte[][]{};
            }
        }

        Script currentScript = txIn.scriptSig;  // combine current ScriptSig and previous ScriptPubKey
        Script combinedScript = currentScript.add(previousScriptPubKey);
        Interpreter interpreter = new Interpreter(combinedScript, z, witness, new ScriptError(), true);
        boolean result = interpreter.evaluateScript();  // evaluate the combined script
        ScriptError scriptError = interpreter.getScriptError(); // TODO set & check correct error code
        return result;
    }

    public boolean verify() {
        // verify this transaction
        if (fee() < 0) {
            return false;  // tx is trying to create btc
        }
        // verify each input has a valid scriptsig
        for (int inputIndex = 0; inputIndex < getDeserializedInputs().length; inputIndex++) {
            if (!verifyInput(inputIndex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Signs all TxIns for this tx, and re-serializes the tx.txInputs, field, saving the calling object the trouble.
     *
     * @param privateKey
     * @return true if all signed inputs pass verification, false otherwise
     */
    public boolean signAllInputs(Secp256k1PrivateKey privateKey) {
        int[] inputIndexes = IntStream.range(0, getDeserializedInputs().length).toArray();
        return signInputs(inputIndexes, privateKey);
    }

    /**
     * Signs TxIns for given inputIndexes, and re-serializes the tx.txInputs, field, saving the calling object the trouble.
     *
     * @param inputIndexes
     * @param privateKey
     * @return true if all signed inputs pass verification, false otherwise
     */
    public boolean signInputs(int[] inputIndexes, Secp256k1PrivateKey privateKey) {
        for (int inputIndex : inputIndexes) {
            if (!signInput(inputIndex, privateKey)) {
                log.error("Failed to verify signed tx input[{}] ", inputIndex);
                return false;
            }
        }
        serializeSignedTxInputs();
        return true;
    }

    /**
     * Signs then verifies TxIn[inputIndex], but does not re-serializes the tx.txInputs.
     *
     * @param inputIndex
     * @param privateKey
     * @return true if signed input passes verification, false otherwise
     */
    private boolean signInput(int inputIndex, Secp256k1PrivateKey privateKey) {
        // Signs the input using the private key
        //
        // def sign_input(self, input_index, private_key):
        //        # get the signature hash (z)
        //        z = self.sig_hash(input_index)
        //        # get der signature of z from private key
        //        der = private_key.sign(z).der()
        //        # append the SIGHASH_ALL to der (use SIGHASH_ALL.to_bytes(1, 'big'))
        //        sig = der + SIGHASH_ALL.to_bytes(1, 'big')
        //        # calculate the sec
        //        sec = private_key.point.sec()
        //        # initialize a new script with [sig, sec] as the cmds
        //        script_sig = Script([sig, sec])
        //        # change input's script_sig to new script
        //        self.tx_ins[input_index].script_sig = script_sig
        //        # return whether sig is valid using self.verify_input
        //        return self.verify_input(input_index)
        BigInteger z = sigHash(inputIndex);
        byte[] der = privateKey.sign(z).getDer();
        byte[] sig = concatenate.apply(der, SIGHASH_ALL.bigEndian());
        byte[] sec = privateKey.getPublicKey().getSec(true);
        byte[][] cmds = new byte[][]{sig, sec};
        Script scriptSig = new Script(cmds);
        TxIn txIn = getDeserializedInputs()[inputIndex];
        txIn.scriptSig = scriptSig;
        log.info("signed input txIn[{}] {}", inputIndex, txIn);
        return verifyInput(inputIndex);
    }

    public boolean isCoinbase() {
        TxIn[] previousInputs = getDeserializedInputs();
        if (previousInputs.length == 1) {
            TxIn input = previousInputs[0];
            // input.scriptSig.toString();  // TODO fix bug in script.toString [asm()] throwing exception in debugger
            return Arrays.equals(input.previousTransactionId, COINBASE_TX_ID)
                    && Arrays.equals(input.previousTransactionIndex, COINBASE_PREV_TX_ID_0xFFFFFFFF);
        }
        return false;
    }

    public Optional<Integer> coinbaseHeight() {
        if (!isCoinbase()) {
            return Optional.empty();
        } else {
            byte[] scriptCmd0 = getDeserializedInputs()[0].scriptSig.getCmds()[0];
            if (scriptCmd0.length > Integer.BYTES) {
                throw new RuntimeException("ScriptSig cmd0.length " + scriptCmd0.length
                        + " > 4 does not represent an integer block height");
            }
            byte[] height = (scriptCmd0.length < Integer.BYTES)
                    ? resizeAndReverse.apply(scriptCmd0, Integer.BYTES)
                    : reverse.apply(scriptCmd0);
            return Optional.of(bytesToInt.apply(height));
        }
    }

    public int getDeserializedVersion() {
        if (deserializedVersion == 0) {
            deserializedVersion = bytesToInt.apply(reverse.apply(this.version));
        }
        return deserializedVersion;
    }


    public TxIn[] getDeserializedInputs() {
        if (deserializedInputs == null) {
            deserializedInputs = parser.parseInputs();
        }
        return deserializedInputs;
    }

    public TxOut[] getDeserializedOutputs() {
        if (deserializedOutputs == null) {
            deserializedOutputs = parser.parseOutputs();
        }
        return deserializedOutputs;
    }


    public int getDeserializedLocktime() {
        if (deserializedLocktime == 0) {
            deserializedLocktime = bytesToInt.apply(reverse.apply(this.locktime));
        }
        return deserializedLocktime;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getTxInputs() {
        return txInputs;
    }

    public byte[] getTxOutputs() {
        return txOutputs;
    }

    public byte[] getLocktime() {
        return locktime;
    }

    public boolean isTestnet() {
        return isTestnet.test(networkType);
    }

    public boolean isMainnet() {
        return isMainnet.test(networkType);
    }

    public boolean isRegtest() {
        return isRegtest.test(networkType);
    }

    @Override
    public String toString() {
        return "Tx{\n" +
                "  version=" + HEX.encode(version) + "\n" +
                ", txInputs=" + HEX.encode(txInputs) + "\n" +
                ", txOutputs=" + HEX.encode(txOutputs) + "\n" +
                ", locktime=" + HEX.encode(locktime) + "\n" +
                ", networkType=" + networkType + "\n"
                + "}";
    }
}
