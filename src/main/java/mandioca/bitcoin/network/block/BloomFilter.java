package mandioca.bitcoin.network.block;

import mandioca.bitcoin.network.message.FilterLoadMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.Predicate;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.ByteCompareFunctions.isOne;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.block.Murmur3.murmurHash3;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

// SEE https://bitco.in/en/developer-reference#filterclear
// SEE https://github.com/bitcoinj/bitcoinj/blob/806afa04419ebdc3c15d5adf065979aa7303e7f6/core/src/main/java/org/bitcoinj/core/BloomFilter.java
// SEE https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp

// MURMUR:
// # Caution: Jimmy Song's Python algorithm is returning an unsigned python integer (>=0)
// # while standard Java solutions return an 32bit signed integer (2 complement)
// https://stackoverflow.com/questions/13305290/is-there-a-pure-python-implementation-of-murmurhash#15754791

public class BloomFilter {

    private static final Logger log = LoggerFactory.getLogger(BloomFilter.class);

    private static final Function<Integer, Integer[]> bitFieldIndexes = (i) -> {
        BigInteger[] indexes = BigInteger.valueOf(i)
                .divideAndRemainder(BigInteger.valueOf(Byte.SIZE));
        return new Integer[]{indexes[0].intValue(), indexes[1].intValue()};
    };

    private static final Predicate<Byte> isSet = isOne;

    private final int size;
    private final int functionCount;
    private final BigInteger tweak;
    private final byte[] bitField;

    public BloomFilter(int size, int functionCount, int tweak) {
        this.size = size;
        this.functionCount = functionCount;
        this.tweak = BigInteger.valueOf(tweak);
        this.bitField = emptyArray.apply(size * Byte.SIZE);
    }

    public void add(byte[] item) {
        if (log.isDebugEnabled()) {
            log.debug("adding filter item {}", HEX.encode(item));
        }
        for (int i = 0; i < functionCount; i++) {
            int hash = murmurHash3(bitField, tweak.intValue(), i, item);
            int bit = BigInteger.valueOf(hash).mod(BigInteger.valueOf(bitField.length)).intValue();
            bitField[bit] = 0x01;
        }
    }

    public byte[] filterBytes() {
        return bitFieldToBytes(bitField);
    }

    public byte[] filterLoad() {
        return filterLoad(1);
    }

    public byte[] filterLoad(int flag) {
        byte[] payloadSizeVarint = VARINT.encode(size);
        byte[] payload = filterPayload(flag);
        ByteBuffer byteBuffer = borrowBuffer.apply(payloadSizeVarint.length + payload.length);
        try {
            byteBuffer.put(payloadSizeVarint);
            byteBuffer.put(payload);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer.clear());
        }
    }

    public int getFunctionCount() {
        return functionCount;
    }

    public BigInteger getTweak() {
        return tweak;
    }

    public byte[] getBitField() {
        return bitField;
    }

    private byte[] filterPayload(int flag) {
        return new FilterLoadMessage(
                filterBytes(),
                reverse.apply(intToBytes.apply(functionCount)),
                reverse.apply(intToBytes.apply(tweak.intValue())),
                ((flag == 0)) ? ZERO_BYTE : ONE_BYTE)
                .serialize();
    }

    public static byte[] bitFieldToBytes(byte[] bitField) {
        if (bitField.length % Byte.SIZE != 0) {
            throw new IllegalStateException("bit field does not have a length that is divisible by 8");
        }
        byte[] result = emptyArray.apply(bitField.length / Byte.SIZE);
        for (int i = 0; i < bitField.length; i++) {
            Integer[] indexes = bitFieldIndexes.apply(i);
            int byteIndex = indexes[0];
            int bitIndex = indexes[1];
            if (isSet.test(bitField[i])) {
                result[byteIndex] |= 1 << bitIndex;
            }
        }
        return result;
    }

    public static byte[] bytesToBitField(byte[] bytes) {
        int bitFieldSize = bytes.length * Byte.SIZE;
        byte[] bitField = emptyArray.apply(bitFieldSize);
        int bitFieldIndex = 0;
        for (byte aByte : bytes) {
            for (int i = 0; i < Byte.SIZE; i++) {
                bitField[bitFieldIndex++] = (byte) (aByte & 1);
                aByte >>= 1;
            }
        }
        return bitField;
    }
}
