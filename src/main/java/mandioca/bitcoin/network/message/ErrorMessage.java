package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.network.message.MessageType.ERROR;

public class ErrorMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] payload;

    public ErrorMessage(byte[] payload) {
        this.payload = payload;
        this.messageType = ERROR;
    }

    public byte[] serialize() {
        return payload;
    }
}
