package mandioca.bitcoin.transaction;

import java.util.Arrays;

public class BlockcypherTransaction extends AbstractTransaction implements Transaction {

    private String blockHash;
    private long blockHeight;
    private int blockIndex;
    private String hash;
    private String hex;
    private String[] addresses;
    private long total;
    private int fees;
    private int size;
    private String preference;
    private String relayedBy;
    private String confirmed; // DateTime
    private String received;  // DateTime
    private int ver;
    private long lockTime;
    private boolean doubleSpend;
    private int vinSize;
    private int voutSize;
    private long confirmations;
    private int confidence;
    // private TxIn[] inputs;
    // private TxOut[] outputs;

    public BlockcypherTransaction() {
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getFees() {
        return fees;
    }

    public void setFees(int fees) {
        this.fees = fees;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getRelayedBy() {
        return relayedBy;
    }

    public void setRelayedBy(String relayedBy) {
        this.relayedBy = relayedBy;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public String getReceived() {
        return received;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public boolean isDoubleSpend() {
        return doubleSpend;
    }

    public void setDoubleSpend(boolean doubleSpend) {
        this.doubleSpend = doubleSpend;
    }

    public int getVinSize() {
        return vinSize;
    }

    public void setVinSize(int vinSize) {
        this.vinSize = vinSize;
    }

    public int getVoutSize() {
        return voutSize;
    }

    public void setVoutSize(int voutSize) {
        this.voutSize = voutSize;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "BlockcypherTransaction{" + '\n' +
                "  blockHash=" + blockHash + '\n' +
                ", blockHeight=" + blockHeight + '\n' +
                ", blockIndex=" + blockIndex + '\n' +
                ", hash=" + hash + '\n' +
                ", hex=" + hex + '\n' +
                ", addresses=" + Arrays.toString(addresses) + '\n' +
                ", total=" + total + '\n' +
                ", fees=" + fees + '\n' +
                ", size=" + size + '\n' +
                ", preference=" + preference + '\n' +
                ", relayedBy=" + relayedBy + '\n' +
                ", confirmed=" + confirmed + '\n' +
                ", received=" + received + '\n' +
                ", ver=" + ver + '\n' +
                ", lockTime=" + lockTime + '\n' +
                ", doubleSpend=" + doubleSpend + '\n' +
                ", vinSize=" + vinSize + '\n' +
                ", voutSize=" + voutSize + '\n' +
                ", confirmations=" + confirmations + '\n' +
                ", confidence=" + confidence + '\n' +
                '}';
    }
}
