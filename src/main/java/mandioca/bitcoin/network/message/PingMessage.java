package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.network.message.MessageType.PING;

/**
 * The ping message is sent primarily to confirm that the TCP/IP connection is still valid. An error in transmission
 * is presumed to be a closed connection and the address is removed as a current peer.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#ping">https://en.bitcoin.it/wiki/Protocol_documentation#ping</a>
 */
public final class PingMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] nonce; // 8 bytes, little endian

    public PingMessage(byte[] nonce) {
        this.nonce = (nonce == null) ? getNewNonce() : nonce;
        this.messageType = PING;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public byte[] serialize() {
        return nonce;
    }
}
