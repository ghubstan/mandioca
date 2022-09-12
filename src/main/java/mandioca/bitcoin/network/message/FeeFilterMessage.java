package mandioca.bitcoin.network.message;

import java.nio.ByteBuffer;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToLong;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.message.MessageType.FEEFILTER;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;

/**
 * The payload is always 8 bytes long and it encodes 64 bit integer value (LSB / little endian) of feerate.
 * The value represents a minimal fee and is expressed in satoshis per 1000 bytes.
 * <p>
 * Upon receipt of a "feefilter" message, the node will be permitted, but not required, to filter transaction invs
 * for transactions that fall below the feerate provided in the feefilter message interpreted as satoshis per kilobyte.
 * <p>
 * The fee filter is additive with a bloom filter for transactions so if an SPV client were to load a bloom filter and
 * send a feefilter message, transactions would only be relayed if they passed both filters.
 * <p>
 * Inv's generated from a mempool message are also subject to a fee filter if it exists.
 * <p>
 * This message is only supported by protocol version >= 70013
 * <p>
 * See <a href="https://github.com/bitcoin/bips/blob/master/bip-0133.mediawiki">BIP 133</a> for more information.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#feefilter">https://en.bitcoin.it/wiki/Protocol_documentation#feefilter</a>
 */
public final class FeeFilterMessage extends AbstractNetworkMessage implements NetworkMessage {

    /**
     * 8-byte integer, little-endian version number.
     * Nodes sending a sendcmpct message MUST currently set this value to 1.
     */
    private final byte[] feeRate;

    public FeeFilterMessage(byte[] feeRate) {
        this.messageType = FEEFILTER;
        this.feeRate = feeRate;
    }

    public long getFeeRate() {
        return bytesToLong.apply(reverse.apply(feeRate));
    }

    public static long getFeeRate(byte[] feeRate) {
        return bytesToLong.apply(reverse.apply(feeRate));
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = borrowBuffer.apply(feeRate.length);
        try {
            byteBuffer.put(feeRate);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer);
        }
    }
}
