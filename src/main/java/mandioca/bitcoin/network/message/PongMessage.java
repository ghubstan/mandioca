package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.network.message.MessageType.PONG;

/**
 * The pong message is sent in response to a ping message.
 * In modern protocol versions, a pong response is generated using a nonce included in the ping.
 */
public final class PongMessage extends AbstractNetworkMessage implements NetworkMessage {


    private final byte[] nonce; // 8 bytes, little endian

    public PongMessage(byte[] nonce) {
        this.nonce = (nonce == null) ? getNewNonce() : nonce;
        this.messageType = PONG;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public byte[] serialize() {
        return nonce;
    }
}
