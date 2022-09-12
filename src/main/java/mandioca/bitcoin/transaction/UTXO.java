package mandioca.bitcoin.transaction;

/**
 * Convenient cache of prevTxId, prevTxIndex, TxOut, TxOut.amountAsLong for tx querying, creation and testing.
 */
public final class UTXO {

    private final String prevTxId;
    private final int prevIndex;
    private final TxOut txOut;
    private final long amount;

    public UTXO(String prevTxId, int prevIndex, TxOut txOut, long amount) {
        this.prevTxId = prevTxId;
        this.txOut = txOut;
        this.prevIndex = prevIndex;
        this.amount = amount;
    }

    public String getPrevTxId() {
        return prevTxId;
    }

    public TxOut getTxOut() {
        return txOut;
    }

    public int getPrevIndex() {
        return prevIndex;
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "UTXO{" +
                "prevTxId='" + prevTxId + '\'' +
                ", prevIndex=" + prevIndex +
                ", txOut=" + txOut +
                ", amount=" + amount +
                '}';
    }
}
