package mandioca.bitcoin.network.block;

import mandioca.bitcoin.function.BigIntegerFunctions;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.System.arraycopy;
import static mandioca.bitcoin.function.BigIntegerFunctions.isLessThan;
import static mandioca.bitcoin.function.BigIntegerFunctions.wrap;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.ByteCompareFunctions.isGreaterThan;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.function.TimeFunctions.TWO_WEEKS_AS_SECONDS;

final class BlockHelper {

    static final byte[] BYTES_0xFFFF = new byte[]{(byte) 0xff, (byte) 0xff};

    // the highest possible getTarget (getDifficulty 1)
    // 0x00ffff * 2**(8*(0x1d - 3)) = 0x00000000FFFF0000000000000000000000000000000000000000000000000000
    // see https://en.bitcoin.it/wiki/Difficulty
    static final BigInteger MAX_TARGET =
            wrap.apply(bytesToInt.apply(BYTES_0xFFFF)).multiply(wrap.apply(256).pow(0x1d - 3));

    // represents lowest bits for mainnet and testnet3, but don't know about regtest
    // see https://en.bitcoin.it/wiki/Difficulty
    private static final BigInteger DEFAULT_LOWEST_BITS =
            wrap.apply(bytesToInt.apply(BYTES_0xFFFF)).multiply(wrap.apply(256).pow(0x1d - 3));

    private final Function<byte[], BigInteger> calcBitsToTargetCoefficient = (bits) -> {
        byte[] c = new byte[bits.length - 1];  // last 3 bytes of bits, little endian
        arraycopy(bits, 0, c, 0, c.length);
        return new BigInteger(1, reverse.apply(c));
    };

    private final BigInteger lowestBits;

    BlockHelper() {
        this.lowestBits = DEFAULT_LOWEST_BITS;
    }

    Predicate<Integer> isBip9 = (v) -> (v >> 29) == 1;
    Predicate<Integer> isBip91 = (v) -> (v >> 4 & 1) == 1;
    Predicate<Integer> isBip141 = (v) -> (v >> 1 & 1) == 1;

    boolean checkProofOfWork(BlockHeader blockHeader) {
        // A valid proof of work is a hash of a block header as a little endian integer is below the getTarget number.
        byte[] hash = hash256.apply(blockHeader.serialize());
        BigInteger proof = new BigInteger(1, reverse.apply(hash));
        BigInteger target = bitsToTarget(blockHeader.bits);
        return isLessThan.apply(proof, target);
    }

    BigInteger difficulty(BigInteger target) {
        //  Returns the block getDifficulty based on the bits.
        //  Difficulty is (getTarget of lowest getDifficulty) / (self's getTarget).
        //  Lowest getDifficulty has bits that equal 0xffff001d
        return lowestBits.divide(target);
    }

    int getTimeDifferential(Block block, Block previousBlock) {
        return block.getBlockHeader().getTimestampInt() - previousBlock.getBlockHeader().getTimestampInt();
    }

    BigInteger bitsToTarget(byte[] bits) {
        int exponent = byteToInt.apply(bits[3]);                            // e = last byte of bits field
        BigInteger coefficient = calcBitsToTargetCoefficient.apply(bits);   // c = last 3 bytes of bits, little endian
        return coefficient.multiply(wrap.apply(256).pow(exponent - 3));
    }

    byte[] calcNewBits(byte[] previousBits, int timeDifferential) {
        // Calculates the new bits given a 2016-block time differential and the previous bits
        // def calculate_new_bits(previous_bits, time_differential):
        //
        //    # if the time differential is greater than 8 weeks, set to 8 weeks
        //    if time_differential > TWO_WEEKS * 4:
        //        time_differential = TWO_WEEKS * 4
        //
        //    # if the time differential is less than half a week, set to half a week
        //    if time_differential < TWO_WEEKS // 4:
        //        time_differential = TWO_WEEKS // 4
        //
        //    # the new getTarget is the previous getTarget * time differential / two weeks
        //    new_target = bits_to_target(previous_bits) * time_differential // TWO_WEEKS
        //
        //    # if the new getTarget is bigger than MAX_TARGET, set to MAX_TARGET
        //    if new_target > MAX_TARGET:
        //        new_target = MAX_TARGET
        //    # convert the new getTarget to bits
        //    return target_to_bits(new_target)

        if (timeDifferential < TWO_WEEKS_AS_SECONDS / 4) {
            timeDifferential = TWO_WEEKS_AS_SECONDS / 4;
        }
        if (timeDifferential > TWO_WEEKS_AS_SECONDS * 4) {
            timeDifferential = TWO_WEEKS_AS_SECONDS * 4;
        }

        BigInteger previousBitsToTarget = bitsToTarget(previousBits);
        BigInteger newTarget = (previousBitsToTarget.multiply(BigInteger.valueOf(timeDifferential)))
                .divide(BigInteger.valueOf(TWO_WEEKS_AS_SECONDS));

        if (BigIntegerFunctions.isGreaterThan.apply(newTarget, MAX_TARGET)) {
            newTarget = MAX_TARGET;
        }

        return targetToBits(newTarget);
    }

    byte[] targetToBits(BigInteger target) {
        // Turns a getTarget integer back into bits, which is 4 bytes
        // def target_to_bits(getTarget):
        //    '''Turns a getTarget integer back into bits, which is 4 bytes'''
        //    raw_bytes = getTarget.to_bytes(32, 'big')
        //    # get rid of leading 0's
        //    raw_bytes = raw_bytes.lstrip(b'\x00')
        //    if raw_bytes[0] > 0x7f:
        //        # if the first bit is 1, we have to start with 00
        //        exponent = len(raw_bytes) + 1
        //        coefficient = b'\x00' + raw_bytes[:2]
        //    else:
        //        # otherwise, we can show the first 3 bytes
        //        # exponent is the number of digits in base-256
        //        exponent = len(raw_bytes)
        //        # coefficient is the first 3 digits of the base-256 number
        //        coefficient = raw_bytes[:3]
        //    # we've truncated the number after the first 3 digits of base-256
        //    new_bits = coefficient[::-1] + bytes([exponent])
        //    return new_bits
        byte[] raw = dropLeadingZeros.apply(bigIntToUnsignedByteArray.apply(target));
        boolean firstCoefficientBitIsSet = isGreaterThan.apply(raw[0], (byte) 0x7f);  // 0x7f = 127
        int exponent = firstCoefficientBitIsSet ? raw.length + 1 : raw.length;
        ByteBuffer coefficientBuffer = ByteBuffer.allocate(3);  // todo borrow this buffer, declare final field at top
        if (firstCoefficientBitIsSet) {
            coefficientBuffer.put(ZERO_BYTE);
            coefficientBuffer.put(raw, 0, 2);
        } else {
            coefficientBuffer.put(raw, 0, 3);
        }
        byte[] coefficient = coefficientBuffer.array();
        return concatenate.apply(reverse.apply(coefficient), new byte[]{(byte) exponent});
    }

}
