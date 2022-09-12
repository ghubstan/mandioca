package mandioca.bitcoin.transaction;

import mandioca.bitcoin.script.Script;

import java.nio.ByteBuffer;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToLong;
import static mandioca.bitcoin.function.EndianFunctions.reverse;

public class TxOut implements Transaction {

    private final byte[] amount;        // in satoshis, serialized (stored) as a little endian byte[8] array (long)
    private final Script scriptPubKey;

    public TxOut(byte[] amount, Script scriptPubKey) {
        this.amount = amount;   // comes in as big endian, but always serialized as little endian
        this.scriptPubKey = scriptPubKey;
    }

    /**
     * Returns the byte serialization of this transaction output.
     *
     * @return byte[]
     */
    public byte[] serialize() {
        byte[] serializedScriptPubKey = scriptPubKey.serialize(); // need now for buffer.alloc
        ByteBuffer bb = ByteBuffer.allocate(amount.length + serializedScriptPubKey.length);
        bb.put(reverse.apply(amount));      // amount is serialized as little endian
        bb.put(serializedScriptPubKey);     // a var length field preceded by a varint defining the field length
        return bb.array();
    }

    public byte[] getAmount() {
        return amount;
    }

    public long getAmountAsLong() {
        return bytesToLong.apply(amount);
    }

    public Script getScriptPubKey() {
        return scriptPubKey;
    }

    @Override
    public String toString() {
        return "TxOut{ amount=" + bytesToLong.apply(amount) + ", scriptPubKey=" + scriptPubKey.asm() + " }";
    }

    //
    // Null Data Transaction Type
    //
    // SEE https://bitcoin.org/en/glossary/null-data-transaction
    // Definition
    // A transaction type relayed and mined by default in Bitcoin Core 0.9.0 and later that adds arbitrary data to a
    // provably unspendable pubkey script that full nodes don’t have to store in their UTXO database.
    //
    // Synonyms
    // Null data transaction
    //
    // OP_RETURN transaction
    //
    // Data carrier transaction
    //
    // Not To Be Confused With
    // OP_RETURN (an opcode used in one of the outputs in an OP_RETURN transaction)
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
    //(Null data scripts cannot be spent, so there's no signature script.)
    //Bitcoin Core 0.11.x increases this default to 80 bytes, with the other rules remaining the same.
    //
    // Bitcoin Core 0.12.0 defaults to relaying and mining null data outputs with up to 83 bytes with any number of data
    // pushes, provided the total byte limit is not exceeded. There must still only be a single null data output and it
    // must still pay exactly 0 satoshis.
    //
    // The -datacarriersize Bitcoin Core configuration option allows you to set the maximum number of bytes in null data
    // outputs that you will relay or mine.
}
