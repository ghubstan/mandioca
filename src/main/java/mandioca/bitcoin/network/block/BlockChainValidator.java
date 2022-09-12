package mandioca.bitcoin.network.block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.BigIntegerFunctions.formatInt;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.TimeFunctions.*;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.NetworkProperties.REGTEST_GENESIS_LOWEST_BITS;
import static mandioca.bitcoin.network.NetworkType.isRegtest;
import static mandioca.bitcoin.network.block.GenesisBlockHeader.genesisBlockHeader;
import static mandioca.bitcoin.network.block.GenesisBlockHeader.proofOfWorkLimit;
import static mandioca.bitcoin.util.HexUtils.HEX;


// It seems that full regtest blk validation won't work for data sets over 2016 blocks,
// due to the lack of regtest support for correct getDifficulty adjustment. (So stop wasting time w/ calcNewBits())
// SEE https://bitcointalk.org/index.php?topic=1065504.0
// SEE https://en.bitcoin.it/wiki/Block_hashing_algorithm
// SEE https://en.bitcoin.it/wiki/Target
// SEE https://github.com/bitcoin-labs/bitcoinj-minimal BlockChain.java

public final class BlockChainValidator {

    private static final Logger log = LoggerFactory.getLogger(BlockChainValidator.class);

    // BIP 0009 requires 95% of blocks signal readiness in a given 2,016 block epoch (diff adjust period) before soft fork feature is activated.
    private static final int DIFFICULTY_ADJUSTMENT_PERIOD = TWO_WEEKS_AS_SECONDS / TEN_MINUTES_AS_SECONDS;    // 2016

    public void validate(List<BlockHeader> blockHeaders) throws InvalidBlockException {
        long t0 = currentTimeMillis();
        log.info("validating {} block headers", blockHeaders.size());
        BlockHeader current, previous = genesisBlockHeader.get();
        int firstEpochTimestamp = previous.getTimestampInt();
        int height = 1;
        while (height < blockHeaders.size()) {
            current = blockHeaders.get(height - 1);
            firstEpochTimestamp = doChecksAndAdjustEpochTimestamp(previous, current, height, firstEpochTimestamp);
            if (height % 300_000 == 0) {
                log.info("validated {} blocks...", formatInt.apply(height));
            }
            previous = current;
            height++;
        }
        log.info(getStatsString(blockHeaders.size(), currentTimeMillis() - t0));
    }

    private final Function<Integer, Integer> adjustedTimeDifferential = (timeDiff) -> {
        if (timeDiff < TWO_WEEKS_AS_SECONDS / 4) {
            timeDiff = TWO_WEEKS_AS_SECONDS / 4;
        }
        if (timeDiff > TWO_WEEKS_AS_SECONDS * 4) {
            timeDiff = TWO_WEEKS_AS_SECONDS * 4;
        }
        return timeDiff;
    };

    private final Function<Integer, BigInteger> calcNewDifficulty = (timeDiff) -> {
        BigInteger newDifficulty = ONE;    //  difficulty = difficulty_1_target / current_target (target is 256 bit nbr)
        newDifficulty = newDifficulty.multiply(BigInteger.valueOf(timeDiff));
        newDifficulty = newDifficulty.divide(BigInteger.valueOf(TWO_WEEKS_AS_SECONDS));
        return newDifficulty;
    };

    private final Function<BigInteger, Boolean> isValidDifficulty = (difficulty) -> {
        // regtest blocks have difficulties far below 1, testnet & mainnet difficulties should never be 0
        boolean tooLow = isRegtest.test(NETWORK) ? difficulty.compareTo(ZERO) < 0 : difficulty.compareTo(ZERO) <= 0;
        boolean tooHigh = difficulty.compareTo(proofOfWorkLimit.get()) > 0;
        return !tooLow && !tooHigh;
    };

    private int doChecksAndAdjustEpochTimestamp(
            BlockHeader previous,
            BlockHeader current,
            int height,
            int firstEpochTimestamp)
            throws InvalidBlockException {
        Block block = new Block(current);
        checkProofOfWork(block, height);
        checkOrder(previous, current, height);
        if (height % DIFFICULTY_ADJUSTMENT_PERIOD == 0) {
            checkDifficultyAdjustment(block, previous, firstEpochTimestamp, height);
            firstEpochTimestamp = current.getTimestampInt();
        }
        return firstEpochTimestamp;
    }

    private void checkProofOfWork(Block block, int height)
            throws InvalidBlockException {
        if (!block.checkProofOfWork()) {
            throw new InvalidBlockException("bad proof of work at block " + height + " " + block.id());
        }
    }

    private void checkOrder(BlockHeader previous, BlockHeader current, int height)
            throws InvalidBlockException {
        if (!Arrays.equals(current.getPreviousBlockBigEndian(), previous.hash())) {
            if (isRegtest.test(NETWORK)) {
                // Why would regtest send blks in incorrect order?  It happens intermittently.
                log.warn("discontinuous block at {}  {}", height, current.getHashHex());
            } else {
                throw new InvalidBlockException("discontinuous block at " + height + "  " + current.getHashHex());
            }
        }
    }

    private void checkDifficultyAdjustment(
            Block block,
            BlockHeader previous,
            int firstEpochTimestamp,
            int height)
            throws InvalidBlockException {
        int timeDiff = adjustedTimeDifferential.apply(previous.getTimestampInt() - firstEpochTimestamp);
        checkNewDifficultyCalculation(timeDiff, height);
        checkReceivedDifficulty(block, height);
        checkNewBitsCalculation(block, previous, timeDiff, height);
    }

    private void checkNewDifficultyCalculation(int timeDiff, int height) {
        BigInteger newDifficulty = calcNewDifficulty.apply(timeDiff);
        if (newDifficulty.compareTo(proofOfWorkLimit.get()) > 0) {
            log.warn("block " + height + "'s difficulty hit proof of work limit: {}", newDifficulty.toString(HEX_RADIX));
        }
    }

    private void checkReceivedDifficulty(Block block, int height) throws InvalidBlockException {
        BigInteger rcvdDifficulty = block.getDifficulty();
        if (!isValidDifficulty.apply(rcvdDifficulty)) {
            throw new InvalidBlockException("block " + height + "'s difficulty target is bad: " + rcvdDifficulty.toString());
        }
    }

    private final byte[] regtestLowestBits = reverse.apply(HEX.decode(REGTEST_GENESIS_LOWEST_BITS));

    private void checkNewBitsCalculation(Block block, BlockHeader previous, int timeDiff, int height)
            throws InvalidBlockException {
        byte[] expectedBits = block.calcNewBits(previous.bits, timeDiff);
        if (isRegtest.test(NETWORK)) {
            // regtest bits are always 207fffff
            if (!Arrays.equals(block.getBlockHeader().bits, regtestLowestBits)) {
                log.error("bad bits at block {}: {}, expected bits {}",
                        height, block.getBlockHeader().getBitsHex(), REGTEST_GENESIS_LOWEST_BITS);
            }
        } else {
            if (!Arrays.equals(block.getBlockHeader().bits, expectedBits)) {
                throw new InvalidBlockException("bad bits at block " + height
                        + ":  " + block.getBlockHeader().getBitsHex()
                        + ", expected bits " + HEX.encode(expectedBits));
            }
        }
    }

    private static String getStatsString(int count, long time) {
        return String.format("chain validation stats:  %s block headers validated in %s at rate of %s headers/s",
                formatInt.apply(count),
                durationString.apply(time),
                formatInt.apply((int) (count * 1000L / time)));
    }
}
