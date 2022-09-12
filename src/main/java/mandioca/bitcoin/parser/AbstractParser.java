package mandioca.bitcoin.parser;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToLong;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

abstract class AbstractParser implements Parser {

    private long decodedByteCount = 0;

    @Override
    public long getDecodedByteCount() {
        return decodedByteCount;
    }

    @Override
    public void incrementDecodedByteCount(int n) {
        decodedByteCount += n;
    }


    @Override
    public void decrementDecodedByteCount(int n) {
        decodedByteCount -= n;
    }


    protected long readVarint(byte[] vi) {
        // Varint's value range is 0 to 2^64 - 1
        // TODO test usages (was broken until 2020-01-25 because it was not converting to little endian)
        if (VARINT.firstByteIs253.test(vi)) {          // if byte == 0xfd, next two bytes are the number
            return bytesToLong.apply(reverse.apply(readBytes(2)));
        } else if (VARINT.firstByteIs254.test(vi)) {   // if byte == 0xfe, next four bytes are the number
            return bytesToLong.apply(reverse.apply(readBytes(4)));
        } else if (VARINT.firstByteIs255.test(vi)) {   // if byte == 0xff, next eight bytes are the number
            return bytesToLong.apply(reverse.apply(readBytes(8)));
        } else { // anything else is just the int
            return bytesToLong.apply(vi);
        }
    }
}
