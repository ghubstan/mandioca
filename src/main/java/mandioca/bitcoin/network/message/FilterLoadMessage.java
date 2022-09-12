package mandioca.bitcoin.network.message;

import mandioca.bitcoin.network.block.BloomFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.message.MessageType.FILTERLOAD;

/**
 * Set a bloom filter.
 * <p>
 * Upon receiving a filterload command, the remote peer will immediately restrict the broadcast transactions it
 * announces (in inv packets) to transactions matching the filter.  The flags control the update behaviour of the
 * matching algorithm.
 * <p>
 * Bloom filtering of connections as defined in BIP 0037.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#filterload.2C_filteradd.2C_filterclear.2C_merkleblock">
 * https://en.bitcoin.it/wiki/Protocol_documentation#filterload.2C_filteradd.2C_filterclear.2C_merkleblock</a>
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki">BIP 0037</a>
 */
public class FilterLoadMessage extends AbstractNetworkMessage implements NetworkMessage {

    // The filterload command is defined as follows:
    //Field Size 	Description 	Data type 	Comments
    // ? 	filter 	uint8_t[] 	The filter itself is simply a bit field of arbitrary byte-aligned size. The maximum size is 36,000 bytes.
    // 4 	nHashFuncs 	uint32_t 	The number of hash functions to use in this filter. The maximum value allowed in this field is 50.
    // 4 	nTweak 	uint32_t 	A random value to add to the seed value in the hash function used by the bloom filter.
    // 1 	nFlags 	uint8_t 	A set of flags that control how matched items are added to the filter.

    private static final Function<Integer, byte[]> serializeInt = (n) -> reverse.apply(intToBytes.apply(n));

    private final byte[] filter;    // a bit field of arbitrary byte-aligned size;  maximum size is 36,000 bytes
    private final byte[] numHashFunctions; // 4 bytes, little endian # of hash functions to use in this filter;  maximum value is 50
    private final byte[] tweak; // 4 bytes, little endian, random value to add to hash function seed used by bloom filter
    private final byte[] flags; // 1 byte, set of bit flags to control how matched items are added to the filter

    public FilterLoadMessage(byte[] filter, byte[] numHashFunctions, byte[] tweak, byte[] flags) {
        this.messageType = FILTERLOAD;
        this.filter = filter;
        this.numHashFunctions = numHashFunctions;
        this.tweak = tweak;
        this.flags = flags;
    }

    public FilterLoadMessage(BloomFilter bloomFilter) {
        this(bloomFilter.filterBytes(),
                serializeInt.apply(bloomFilter.getFunctionCount()),
                serializeInt.apply(bloomFilter.getTweak().intValue()),
                bloomFilter.getBitField());
        //  this(bloomFilter.filterLoad().serialize(),
        //                serializeInt.apply(bloomFilter.getFunctionCount()),
        //                serializeInt.apply(bloomFilter.getTweak().intValue()),
        //                bloomFilter.getBitField());

    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(filter);
            baos.write(numHashFunctions);
            baos.write(tweak);
            baos.write(flags);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing filterload message", e);
        }
    }

}
