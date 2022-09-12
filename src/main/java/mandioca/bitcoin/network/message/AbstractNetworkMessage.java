package mandioca.bitcoin.network.message;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static mandioca.bitcoin.function.ByteArrayFunctions.longToBytes;
import static mandioca.bitcoin.function.EndianFunctions.reverse;

abstract class AbstractNetworkMessage {

    private static final NonceFactory nonceFactory = new NonceFactory();

    protected static final Function<Long, byte[]> serializeLongTimestamp = (t) -> reverse.apply(longToBytes.apply(t));
    protected static final Supplier<byte[]> serializedLongTimestamp = () -> serializeLongTimestamp.apply(currentTimeMillis() / 1000L);

    protected MessageType messageType;

    public MessageType getMessageType() {
        return this.messageType;
    }

    protected static byte[] getNewNonce() {
        return nonceFactory.getNonce();
    }
}
