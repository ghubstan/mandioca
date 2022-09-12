package mandioca.bitcoin.network.block;

import mandioca.bitcoin.MandiocaTest;
import mandioca.bitcoin.transaction.Tx;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;

import static mandioca.bitcoin.function.BigIntegerFunctions.isGreaterThan;
import static mandioca.bitcoin.function.ByteArrayFunctions.hexToByteArrayInputStream;
import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.function.TimeFunctions.*;
import static mandioca.bitcoin.network.NetworkType.MAINNET;
import static mandioca.bitcoin.network.block.BlockHeader.parse;
import static mandioca.bitcoin.network.block.BlockHelper.MAX_TARGET;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.*;

public class BlockTest extends MandiocaTest {


    private static final Logger log = LoggerFactory.getLogger(BlockTest.class);

    @Test
    public void testIsCoinbase() {
        // From programmingbitcoin/code-ch09/tx.py def test_is_coinbase(self)
        String rawHex = "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff5e03d71b07254d696e656420627920416e74506f6f6c20626a31312f4542312f4144362f43205914293101fabe6d6d678e2c8c34afc36896e7d9402824ed38e856676ee94bfdb0c6c4bcd8b2e5666a0400000000000000c7270000a5e00e00ffffffff01faf20b58000000001976a914338c84849423992471bffb1a54a8d9b1d69dc28a88ac00000000";
        byte[] rawTx = HEX.decode(rawHex);
        Tx tx = Tx.parse(toByteArrayInputStream.apply(rawTx), MAINNET);
        assertTrue(tx.isCoinbase());
    }

    @Test
    public void testCoinbaseHeightNotPresent() {
        // From programmingbitcoin/code-ch09/tx.py def test_coinbase_height(self)
        String rawHex = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccfcf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
        byte[] rawTx = HEX.decode(rawHex);
        Tx tx = Tx.parse(toByteArrayInputStream.apply(rawTx), MAINNET);
        Optional<Integer> height = tx.coinbaseHeight();
        assertFalse(height.isPresent());
    }

    @Test
    public void testCoinbaseHeight465879() {
        // From programmingbitcoin/code-ch09/tx.py def test_coinbase_height(self)
        String rawHex = "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff5e03d71b07254d696e656420627920416e74506f6f6c20626a31312f4542312f4144362f43205914293101fabe6d6d678e2c8c34afc36896e7d9402824ed38e856676ee94bfdb0c6c4bcd8b2e5666a0400000000000000c7270000a5e00e00ffffffff01faf20b58000000001976a914338c84849423992471bffb1a54a8d9b1d69dc28a88ac00000000";
        byte[] rawTx = HEX.decode(rawHex);
        Tx tx = Tx.parse(toByteArrayInputStream.apply(rawTx), MAINNET);
        long expectedHeight = 465879L;
        Optional<Integer> height = tx.coinbaseHeight();
        assertTrue(height.isPresent());
        assertEquals(expectedHeight, height.get().longValue());
    }

    @Test
    public void testBip9() {
        // From programmingbitcoin/code-ch09/block.py def test_bip9(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexStream.apply(blockHeaderRaw));
        Block block = new Block(blockHeader);
        // printSetBits("Bip9 test for set version bits: ", blockHeader.version);
        assertTrue(block.isBip9());

        blockHeaderRaw = "0400000039fa821848781f027a2e6dfabbf6bda920d9ae61b63400030000000000000000ecae536a304042e3154be0e3e9a8220e5568c3433a9ab49ac4cbb74f8df8e8b0cc2acf569fb9061806652c27";
        blockHeader = parse(hexStream.apply(blockHeaderRaw));
        block = new Block(blockHeader);
        // printSetBits("Bip9 test for set version bits: ", blockHeader.version);
        assertFalse(block.isBip9());
    }

    @Test
    public void testBip91() {
        // From programmingbitcoin/code-ch09/block.py def test_bip91(self)
        String blockHeaderRaw = "1200002028856ec5bca29cf76980d368b0a163a0bb81fc192951270100000000000000003288f32a2831833c31a25401c52093eb545d28157e200a64b21b3ae8f21c507401877b5935470118144dbfd1";
        BlockHeader blockHeader = parse(hexStream.apply(blockHeaderRaw));
        Block block = new Block(blockHeader);
        // printSetBits("Bip91 test for set version bits: ", blockHeader.version);
        assertTrue(block.isBip91());

        blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        blockHeader = parse(hexStream.apply(blockHeaderRaw));
        block = new Block(blockHeader);
        // printSetBits("Bip91 test for set version bits: ", blockHeader.version);
        assertFalse(block.isBip91());
    }

    @Test
    public void testBip141() {
        // From programmingbitcoin/code-ch09/block.py def test_bip141(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexStream.apply(blockHeaderRaw));
        Block block = new Block(blockHeader);
        // printSetBits("Bip141 test for set version bits: ", blockHeader.version);
        assertTrue(block.isBip141());

        blockHeaderRaw = "0000002066f09203c1cf5ef1531f24ed21b1915ae9abeb691f0d2e0100000000000000003de0976428ce56125351bae62c5b8b8c79d8297c702ea05d60feabb4ed188b59c36fa759e93c0118b74b2618";
        blockHeader = parse(hexStream.apply(blockHeaderRaw));
        block = new Block(blockHeader);
        // printSetBits("Bip141 test for set version bits: ", blockHeader.version);
        assertFalse(block.isBip141());
    }

    @Test
    public void testTarget() {
        // From programmingbitcoin/code-ch09/block.py def test_target(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexStream.apply(blockHeaderRaw));
        Block block = new Block(blockHeader);
        String expectedTargetHex = "13ce9000000000000000000000000000000000000000000";
        assertEquals(expectedTargetHex, HEX.encode(block.getTarget()));
        //System.out.println("Target: " + block.getTargetHex());
    }

    @Test
    public void testDifficulty() {
        // From programmingbitcoin/code-ch09/block.py def test_difficulty(self)
        String blockHeaderRaw = "020000208ec39428b17323fa0ddec8e887b4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
        BlockHeader blockHeader = parse(hexStream.apply(blockHeaderRaw));
        Block block = new Block(blockHeader);
        BigInteger expectedDifficulty = new BigInteger("888171856257");
        assertEquals(expectedDifficulty, block.getDifficulty());
    }

    @Test
    public void testCheckPow() {
        // From programmingbitcoin/code-ch09/block.py def test_check_pow(self)
        String blockHeaderRaw = "04000000fbedbbf0cfdaf278c094f187f2eb987c86a199da22bbb20400000000000000007b7697b29129648fa08b4bcd13c9d5e60abb973a1efac9c8d573c71c807c56c3d6213557faa80518c3737ec1";
        BlockHeader blockHeader = parse(hexStream.apply(blockHeaderRaw));
        Block block = new Block(blockHeader);
        assertTrue(block.checkProofOfWork());

        blockHeaderRaw = "04000000fbedbbf0cfdaf278c094f187f2eb987c86a199da22bbb20400000000000000007b7697b29129648fa08b4bcd13c9d5e60abb973a1efac9c8d573c71c807c56c3d6213557faa80518c3737ec0";
        blockHeader = parse(hexStream.apply(blockHeaderRaw));
        block = new Block(blockHeader);
        assertFalse(block.checkProofOfWork());
    }

    @Test
    public void testTargetToBits() {
        // From programmingbitcoin/code-ch09/block.py  Exercise 12 (has bug in book's code & wrong answer)
        // Bug fix and correct answer is at https://github.com/jimmysong/programmingbitcoin/issues/165
        String block1HeaderRaw = "000000203471101bbda3fe307664b3283a9ef0e97d9a38a7eacd8800000000000000000010c8aba8479bbaa5e0848152fd3c2289ca50e1c3e58c9a4faaafbdf5803c5448ddb845597e8b0118e43a81d3";
        BlockHeader block1Header = parse(hexStream.apply(block1HeaderRaw));
        Block firstBlock = new Block(block1Header);

        String block2HeaderRaw = "02000020f1472d9db4b563c35f97c428ac903f23b7fc055d1cfc26000000000000000000b3f449fcbe1bc4cfbcb8283a0d2c037f961a3fdf2b8bedc144973735eea707e1264258597e8b0118e5f00474";
        BlockHeader block2Header = parse(hexStream.apply(block2HeaderRaw));
        Block lastBlock = new Block(block2Header);

        int timeDifferential = lastBlock.getTimeDifferential(firstBlock);
        byte[] newTargetBits = lastBlock.calcNewBits(firstBlock.getBlockHeader().getBits(), timeDifferential);
        assertEquals("308d0118", HEX.encode(newTargetBits));
    }


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
