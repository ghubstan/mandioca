package mandioca.bitcoin.transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;
import static mandioca.bitcoin.network.NetworkConstants.SEGWIT_MARKER;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

class TransactionSerializer {

    static final byte[] VERSION_1 = toLittleEndian.apply(1, Integer.BYTES);
    static final byte[] VERSION_2 = toLittleEndian.apply(2, Integer.BYTES);
    static final byte[] VERSION_3 = toLittleEndian.apply(3, Integer.BYTES);
    static final byte[] VERSION_4 = toLittleEndian.apply(4, Integer.BYTES);

    static final byte[] LOCKTIME_0 = new byte[]{0x00, 0x00, 0x00, 0x00};

    final static Function<Integer, byte[]> serializeVersion = (v) -> toLittleEndian.apply(v, Integer.BYTES);
    final static Function<Integer, byte[]> serializeLocktime = (l) -> toLittleEndian.apply(l, Integer.BYTES);
    final static Function<Long, byte[]> serializeAmount = (a) -> toLittleEndian.apply(a, Long.BYTES);

    private static final Function<Transaction, Boolean> isTxIn = (t) -> t instanceof TxIn;
    private static final Function<Transaction, Boolean> isTxOut = (t) -> t instanceof TxOut;

    private final static Function<Integer, byte[]> serializeNumInnerTransactions = (n) -> VARINT.encode(n);

    private Tx tx;

    public TransactionSerializer() {
    }

    public TransactionSerializer init(Tx tx) {
        this.tx = tx;
        return this;
    }

    public byte[] serialize() {
        return tx.isSegwit ? serializeSegwit() : serializePreSegwit();
    }

    byte[] serializeSegwit() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(tx.version);         // tx.version is 4 little endian bytes
            baos.write(SEGWIT_MARKER);      // add the 2 marker bytes
            baos.write(tx.getTxInputs());   // varint encoded # inputs  value is already prepended to tx.txInputs  field
            baos.write(tx.getTxOutputs());  // varint encoded # outputs value is already prepended to tx.txOutputs field
            serializeWitness(baos);
            baos.write(tx.locktime);        // tx.locktime is 4 little endian bytes
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing segwit transaction", e);
        }
    }

    void serializeWitness(ByteArrayOutputStream baos) throws IOException {
        // TODO less verbosity
        for (TxIn txIn : tx.getDeserializedInputs()) {
            byte[][] witness = txIn.witness;
            byte[] witnessLengthVarint = toLittleEndian.apply(witness.length, 1);
            baos.write(witnessLengthVarint);
            for (int i = 0; i < witness.length; i++) {
                byte[] item = witness[i];
                if (item.length == Integer.BYTES) { // python:  type(item) == int
                    baos.write(toLittleEndian.apply(bytesToInt.apply(item), 1));
                } else {
                    byte[] varint = VARINT.encode(item.length);
                    baos.write(concatenate.apply(varint, item));
                }
            }
        }
    }

    byte[] serializePreSegwit() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(tx.version);         // tx.version is 4 little endian bytes
            baos.write(tx.getTxInputs());   // varint encoded # inputs  value is already prepended to tx.txInputs  field
            baos.write(tx.getTxOutputs());  // varint encoded # outputs value is already prepended to tx.txOutputs field
            baos.write(tx.locktime);        // tx.locktime is 4 little endian bytes
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing transaction", e);
        }
    }

    private static void writeInnerTransactionsToStream(Transaction[] transactions, ByteArrayOutputStream baos) throws IOException {
        // For now, use (int) array.length as the number of inputs, but this may hurt later.
        baos.write(serializeNumInnerTransactions.apply(transactions.length));
        Arrays.stream(transactions).forEach(t -> {
            try {
                if (isTxIn.apply(t)) {
                    baos.write(((TxIn) t).serialize());
                } else if (isTxOut.apply(t)) {
                    baos.write(((TxOut) t).serialize());
                } else {
                    throw new IllegalStateException("Error serializing an unknown inner tx type " + t.getClass().getSimpleName());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static byte[] serializeTransactionInputs(TxIn[] txIns) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Warning, an array is bounded in size to Integer.MAX_VALUE.   Will this bite me?
            if (txIns.length > 0) {
                writeInnerTransactionsToStream(txIns, baos);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing tx inputs array", e);
        }
    }

    static byte[] serializeTransactionOutputs(TxOut[] txOuts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (txOuts.length > 0) {
                writeInnerTransactionsToStream(txOuts, baos);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing tx outputs array", e);
        }
    }
}
