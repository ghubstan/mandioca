package mandioca.bitcoin.network.message;

import java.nio.ByteBuffer;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.ByteCompareFunctions.isEqual;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.message.MessageType.SENDCMPCT;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;

/**
 * The sendcmpct message is defined as a message containing a 1-byte integer followed by a 8-byte integer
 * where pchCommand == "sendcmpct".
 * <p>
 * The first integer SHALL be interpreted as a boolean (and MUST have a value of either 1 or 0)
 * <p>
 * The second integer SHALL be interpreted as a little-endian version number. Nodes sending a sendcmpct message
 * MUST currently set this value to 1.
 * <p>
 * Upon receipt of a "sendcmpct" message with the first and second integers set to 1, the node SHOULD announce new
 * blocks by sending a cmpctblock message.
 * <p>
 * Upon receipt of a "sendcmpct" message with the first integer set to 0, the node SHOULD NOT announce new blocks by
 * sending a cmpctblock message, but SHOULD announce new blocks by sending invs or headers, as defined by BIP130.
 * <p>
 * Upon receipt of a "sendcmpct" message with the second integer set to something other than 1, nodes MUST treat the
 * peer as if they had not received the message (as it indicates the peer will provide an unexpected encoding in
 * <p>
 * cmpctblock, and/or other, messages). This allows future versions to send duplicate sendcmpct messages with different
 * versions as a part of a version handshake for future versions.
 * <p>
 * Nodes SHOULD check for a protocol version of >= 70014 before sending sendcmpct messages.
 * <p>
 * Nodes MUST NOT send a request for a MSG_CMPCT_BLOCK object to a peer before having received a sendcmpct message
 * from that peer.
 * <p>
 * This message is only supported by protocol version >= 70014
 * <p>
 * See <a href="https://github.com/bitcoin/bips/blob/master/bip-0152.mediawiki">BIP 152</a> for more information.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#sendcmpct">https://en.bitcoin.it/wiki/Protocol_documentation#sendcmpct</a>
 */
public final class SendCmpctMessage extends AbstractNetworkMessage implements NetworkMessage {

    /**
     * 1-byte integer interpreted as a boolean
     */
    private final byte[] announceUsingCmpctBlk;

    /**
     * 8-byte integer, little-endian version number.
     * Nodes sending a sendcmpct message MUST currently set this value to 1.
     */
    private final byte[] cmpctBlkVersion;

    public SendCmpctMessage(byte[] announceUsingCmpctBlk, byte[] cmpctBlkVersion) {
        this.messageType = SENDCMPCT;
        this.announceUsingCmpctBlk = announceUsingCmpctBlk;
        this.cmpctBlkVersion = cmpctBlkVersion;
    }

    public boolean isUsingCmpctBlk() {
        return isEqual.apply((byte) 0x01, announceUsingCmpctBlk[0]);
    }

    public int getVersion() {
        return bytesToInt.apply(reverse.apply(cmpctBlkVersion));
    }

    public static boolean isUsingCmpctBlk(byte[] serializedSendCmpctMessage) {
        return Boolean.valueOf(String.valueOf(serializedSendCmpctMessage[0]));
    }

    public static long getVersionAsLong(byte[] serializedSendCmpctMessage) {
        byte[] versionBytes = subarray.apply(serializedSendCmpctMessage, 1, serializedSendCmpctMessage.length - 1);
        return bytesToLong.apply(reverse.apply(versionBytes));
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = borrowBuffer.apply(announceUsingCmpctBlk.length + cmpctBlkVersion.length);
        try {
            byteBuffer.put(announceUsingCmpctBlk);
            byteBuffer.put(cmpctBlkVersion);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer);
        }
    }
}
