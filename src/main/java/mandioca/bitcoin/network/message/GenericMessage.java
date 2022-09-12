package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.network.message.MessageType.GENERIC;

/**
 * Multipurpose use, but should eventually be discarded.
 */
@Deprecated
public class GenericMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] payload;
    private final byte[] command;

    public GenericMessage(byte[] command, byte[] payload) {
        this.messageType = GENERIC;
        this.command = command;
        this.payload = payload;
    }

    @Override
    public byte[] serialize() {
        return payload;
    }

    public byte[] getPayload() {
        return payload;
    }
}
