package mandioca.bitcoin.network.message;

import java.util.function.Function;
import java.util.function.Supplier;

public class MessageFactory {

    private static final NonceFactory nonceFactory = new NonceFactory();

    public static byte[] getNonce() {
        return nonceFactory.getNonce();
    }

    public static final Function<byte[], VersionMessage> versionMessageWithNonce = (nonce) ->
            new VersionMessage.VersionMessageBuilder().withTimestamp().withNonce(nonce).build();

    public static final Supplier<PingMessage> pingMessage = () -> new PingMessage(nonceFactory.getNonce());

    public static final Function<byte[], PongMessage> pongMessage = (nonce) -> new PongMessage(nonce);

}
