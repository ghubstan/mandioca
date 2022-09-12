package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.network.message.MessageType.VERACK;

public final class VerAckMessage extends AbstractNetworkMessage implements NetworkMessage {

    public VerAckMessage() {
        this.messageType = VERACK;
    }

    @Override
    public byte[] serialize() {
        return emptyArray.apply(0);
    }
}
