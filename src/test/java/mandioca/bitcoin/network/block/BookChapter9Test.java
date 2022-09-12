package mandioca.bitcoin.network.block;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;

import java.math.BigInteger;

import static mandioca.bitcoin.function.BigIntegerFunctions.isGreaterThan;
import static mandioca.bitcoin.function.ByteArrayFunctions.hexToByteArrayInputStream;
import static mandioca.bitcoin.function.TimeFunctions.*;
import static mandioca.bitcoin.network.block.BlockHeader.parse;
import static mandioca.bitcoin.network.block.BlockHelper.MAX_TARGET;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class BookChapter9Test extends MandiocaTest {

    @Test
    public void testExercise12() {
        // INCORRECT answer in book due to bug described in issue
        //      https://github.com/jimmysong/programmingbitcoin/issues/165
        String block1HeaderRaw = "000000203471101bbda3fe307664b3283a9ef0e97d9a38a7eacd8800000000000000000010c8aba8479bbaa5e0848152fd3c2289ca50e1c3e58c9a4faaafbdf5803c5448ddb845597e8b0118e43a81d3";
        BlockHeader block1Header = parse(hexToByteArrayInputStream.apply(block1HeaderRaw));
        Block firstBlock = new Block(block1Header);

        String block2HeaderRaw = "02000020f1472d9db4b563c35f97c428ac903f23b7fc055d1cfc26000000000000000000b3f449fcbe1bc4cfbcb8283a0d2c037f961a3fdf2b8bedc144973735eea707e1264258597e8b0118e5f00474";
        BlockHeader block2Header = parse(hexToByteArrayInputStream.apply(block2HeaderRaw));
        Block lastBlock = new Block(block2Header);

        int timeDifferential = lastBlock.getTimeDifferential(firstBlock);

        // TODO fix
        if (timeDifferential > EIGHT_WEEKS_AS_SECONDS) {
            // time differential is greater than 8 weeks, set to 8 weeks
            timeDifferential = EIGHT_WEEKS_AS_SECONDS;
        } else if (timeDifferential < THREE_AND_A_HALF_DAYS_AS_SECONDS) {
            // time differential is less than half a week, set to half a week
            timeDifferential = THREE_AND_A_HALF_DAYS_AS_SECONDS;
        }
        BigInteger newTarget = (lastBlock.getTarget().multiply(BigInteger.valueOf(timeDifferential)))
                .divide(BigInteger.valueOf(TWO_WEEKS_AS_SECONDS));
        if (isGreaterThan.apply(newTarget, MAX_TARGET)) {
            newTarget = MAX_TARGET;
        }
        byte[] newBits = lastBlock.targetToBits(newTarget);
        // INCORRECT answer in book due to bug described in issue
        //      https://github.com/jimmysong/programmingbitcoin/issues/165
        //>>> print(new_bits.hex())   80df6217
        assertEquals("308d0118", HEX.encode(newBits)); // Correct answer
        // https://github.com/jimmysong/programmingbitcoin/issues/165
        // A mistake in the code leads to an incorrect answer for the exercise. The first block is used as the last
        // block and the last block is used as the first. This results in a negative time differential thus is caught
        // by the < TWO_WEEKS // 4 statement. This means that what is actually calculated is the minimum getDifficulty
        // adjustment from block 471744 rather than the correct getDifficulty adjustment from block 473759.
        // The correct answer should be 308d0118 which can be seen on a block explorer e.g.
        // https://blockstream.info/block/000000000000000000802ba879f1b7a638dcea6ff0ceb614d91afc8683ac0502
        // OR
        // https://bitcoinchain.com/block_explorer/block/000000000000000000802ba879f1b7a638dcea6ff0ceb614d91afc8683ac0502/
    }

}
