package mandioca.bitcoin.transaction;

import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.script.Script;

import java.nio.ByteBuffer;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToLong;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class TxIn implements Transaction {

    static final byte[] SEQUENCE_0xFEFFFFFF = new byte[]{(byte) 0xfe, (byte) 0xff, (byte) 0xff, (byte) 0xff}; // 4294967294
    static final byte[] SEQUENCE_0xFFFFFFFF = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};  // -1
    static final Script EMPTY_SCRIPT = new Script(new byte[][]{});

    static final Function<Integer, byte[]> serializedPreviousTransactionIndex = (v) -> toLittleEndian.apply(v, 4);
    /**
     * Double hash 256 of previous tx contents (32 bytes, little endian)
     */
    final byte[] previousTransactionId;
    /**
     * Index of output of a previous tx being spent (4 bytes, little endian)
     */
    final byte[] previousTransactionIndex;
    /**
     * Script sig cmds are variable length, little endian, set during signing
     */
    Script scriptSig;
    /**
     * (4 bytes, little endian) kind of like your spleen, an organ with no purpose
     */
    final byte[] sequence;

    /**
     * The witness is a serialization of all witness data of the transaction.
     * <p>
     * Each txin is associated with a witness field. As a result, there is no indication of number of witness fields,
     * as it is implied by the number of txins.
     * <p>
     * Each witness field starts with a compactSize integer to indicate the number of stack items for the corresponding
     * txin. It is then followed by witness stack item(s) for the corresponding txin, if any.
     * <p>
     * Each witness stack item starts with a compactSize integer to indicate the number of bytes of the item..
     * <p>
     * If a txin is not associated with any witness data, its corresponding witness field is an exact 0x00, indicating
     * that the number of witness stack items is zero.
     * <p>
     * Examples of the transaction serialization can be found under the example section of BIP143. Wallet developers
     * may use the examples to test if their implementations correctly parse the new serialization format.
     * See https://en.bitcoin.it/wiki/BIP_0143
     *
     * <p>
     * Only present in segwit transactions, used for tx validation in segwit upgraded nodes.
     * <p>
     * In a p2wpkh transaction, the witness field contains the signature and pubkey, in that order.
     * <p>
     * In a p2wsh transaction, the witness field contains ... TODO
     * <p>
     * TODO
     * See https://bitcoincore.org/en/segwit_wallet_dev
     */
    byte[][] witness;

    public TxIn(byte[] previousTransactionId, byte[] previousTransactionIndex, Script scriptSig, byte[] sequence) {
        this.previousTransactionId = previousTransactionId;
        this.previousTransactionIndex = previousTransactionIndex;
        this.scriptSig = scriptSig;
        this.sequence = sequence;
    }

    public TxIn(byte[] previousTransactionId, byte[] previousTransactionIndex) {
        this.previousTransactionId = previousTransactionId;
        this.previousTransactionIndex = previousTransactionIndex;
        this.scriptSig = EMPTY_SCRIPT;
        this.sequence = SEQUENCE_0xFFFFFFFF;
    }

    /**
     * Returns the byte serialization of this transaction input.
     *
     * @return byte[]
     */
    public byte[] serialize() {
        byte[] scriptSig = this.scriptSig.serialize(); // Need ScriptSig up front for buffer.alloc
        int length = previousTransactionId.length + previousTransactionIndex.length + scriptSig.length + sequence.length;
        ByteBuffer bb = ByteBuffer.allocate(length);
        bb.put(reverse.apply(previousTransactionId));
        bb.put(reverse.apply(previousTransactionIndex));
        bb.put(scriptSig);
        bb.put(reverse.apply(sequence));
        return bb.array();
    }

    public Tx fetchPreviousTx(NetworkType networkType) {
        return TxFetcher.fetchRawTx(HEX.encode(previousTransactionId), true, networkType);
    }

    public byte[] value(NetworkType networkType) {
        Tx tx = fetchPreviousTx(networkType);
        int outputsIndex = bytesToInt.apply(this.previousTransactionIndex);
        return tx.getDeserializedOutputs()[outputsIndex].getAmount();
    }

    public long valueAsLong(NetworkType networkType) {
        return bytesToLong.apply(value(networkType));
    }

    public Script scriptPubKey(NetworkType networkType) {
        Tx tx = fetchPreviousTx(networkType);      // fetch previous script pubkey
        int outputsIndex = bytesToInt.apply(this.previousTransactionIndex);
        return tx.getDeserializedOutputs()[outputsIndex].getScriptPubKey();
    }

    @Override
    public String toString() {
        return "TxIn{" +
                "  previousTransactionId=" + HEX.encode(previousTransactionId) + "\n" +
                ", previousTransactionIndex=" + HEX.encode(previousTransactionIndex) + "\n" +
                ", script=" + (scriptSig == null ? "" : scriptSig.toString()) + "\n" +
                ", sequence=" + HEX.encode(sequence) + "\n" +
                '}';
    }
}
