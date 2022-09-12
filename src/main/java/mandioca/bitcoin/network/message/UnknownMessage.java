package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.network.message.MessageType.UNKNOWN;

// TODO don't wrap payloads > 32 mb in this msg

public final class UnknownMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] payload;

    public UnknownMessage(byte[] payload) {
        this.payload = payload;
        this.messageType = UNKNOWN;
    }

    public byte[] serialize() {
        return payload;
    }
}
