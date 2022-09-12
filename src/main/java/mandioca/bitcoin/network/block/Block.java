package mandioca.bitcoin.network.block;

import java.math.BigInteger;

import static mandioca.bitcoin.util.HexUtils.HEX;

// See https://en.bitcoin.it/wiki/Block

public class Block {

    private final BlockHeader blockHeader;
    private final BlockHelper blockHelper;

    public Block(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
        this.blockHelper = new BlockHelper();
    }

    public String id() {
        return HEX.to64DigitPaddedHex(blockHeader.hash());  // hex(header.hash()); little endian; is next.previous-block
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public boolean isBip9() {
        return blockHelper.isBip9.test(blockHeader.getVersionInt());
    }

    public boolean isBip91() {
        return blockHelper.isBip91.test(blockHeader.getVersionInt());
    }

    public boolean isBip141() {
        return blockHelper.isBip141.test(blockHeader.getVersionInt());
    }

    public BigInteger bitsToTarget(byte[] bits) {
        return blockHelper.bitsToTarget(bits);
    }

    public BigInteger getTarget() {
        return blockHelper.bitsToTarget(blockHeader.bits);
    }

    public String getTargetHex() {
        return HEX.to64DigitPaddedHex(getTarget());
    }

    public int getTimeDifferential(Block previousBlock) {
        return blockHelper.getTimeDifferential(this, previousBlock);
    }

    public byte[] targetToBits(BigInteger target) {
        return blockHelper.targetToBits(target);
    }

    public BigInteger getDifficulty() {
        return blockHelper.difficulty(getTarget());
    }

    public boolean checkProofOfWork() {
        return blockHelper.checkProofOfWork(blockHeader);
    }

    public byte[] calcNewBits(byte[] previousBits, int timeDifferential) {
        return blockHelper.calcNewBits(previousBits, timeDifferential);
    }

    @Override
    public String toString() {
        return "Block{" + "\n" +
                "  id=" + id() + "\n" +
                ", blockHeader=" + blockHeader +
                '}';
    }
}
