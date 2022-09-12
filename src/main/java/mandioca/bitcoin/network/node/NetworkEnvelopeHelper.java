package mandioca.bitcoin.network.node;

import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.network.message.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static mandioca.bitcoin.network.NetworkConstants.VERACK_MESSAGE;
import static mandioca.bitcoin.network.message.MessageFactory.pongMessage;
import static mandioca.bitcoin.network.message.MessageType.*;


// TODO change name to NetworkEnvelopeFactory and make all methods static
final class NetworkEnvelopeHelper {

    protected final Predicate<NetworkEnvelope> isError = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(ERROR);
    protected final Predicate<NetworkEnvelope> isHeaders = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(HEADERS);
    protected final Predicate<NetworkEnvelope> isInv = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(INV);
    protected final Predicate<NetworkEnvelope> isMerkleBlock = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(MERKLEBLOCK);
    protected final Predicate<NetworkEnvelope> isPing = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(PING);
    protected final Predicate<NetworkEnvelope> isPong = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(PONG);
    protected final Predicate<NetworkEnvelope> isSendCmpct = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(SENDCMPCT);
    protected final Predicate<NetworkEnvelope> isSendHeaders = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(SENDHEADERS);
    protected final Predicate<NetworkEnvelope> isTx = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(TX);
    protected final Predicate<NetworkEnvelope> isVersion = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(VERSION);
    protected final Predicate<NetworkEnvelope> isVerAck = (envelope) -> getMessageType.apply(envelope.getNetworkCommand()).equals(VERACK);

    // these helper functions require a NETWORK param to ease offline testing

    protected final BiFunction<GetDataMessage, NetworkType, byte[]> getDataPayload = (msg, networkType) -> {
        NetworkEnvelope envelope = new NetworkEnvelope(GETDATA.command(), msg.serialize(), networkType);
        return envelope.serialize();
    };

    protected final BiFunction<GetHeadersMessage, NetworkType, byte[]> headersPayload = (msg, networkType) -> {
        NetworkEnvelope envelope = new NetworkEnvelope(msg.getMessageType().command(), msg.serialize(), networkType);
        return envelope.serialize();
    };

    protected final Function<NetworkType, byte[]> verackPayload = (networkType) -> {
        NetworkEnvelope envelope =
                new NetworkEnvelope(VERACK_MESSAGE.getMessageType().command(), VERACK_MESSAGE.serialize(), networkType);
        return envelope.serialize();
    };

    protected final BiFunction<byte[], NetworkType, byte[]> versionPayload = (nonce, networkType) -> {
        VersionMessage vm = MessageFactory.versionMessageWithNonce.apply(nonce);
        NetworkEnvelope envelope = new NetworkEnvelope(vm.getMessageType().command(), vm.serialize(), networkType);
        return envelope.serialize();
    };

    protected final BiFunction<PingMessage, NetworkType, byte[]> pingPayload = (ping, networkType) -> {
        NetworkEnvelope envelope = new NetworkEnvelope(ping.getMessageType().command(), ping.serialize(), networkType);
        return envelope.serialize();
    };

    protected final BiFunction<byte[], NetworkType, byte[]> pongPayload = (nonce, networkType) -> {
        PongMessage pong = pongMessage.apply(nonce);
        NetworkEnvelope envelope = new NetworkEnvelope(pong.getMessageType().command(), pong.serialize(), networkType);
        return envelope.serialize();
    };

    /*
    // TODO fix bug
    protected final BiFunction<FilterLoadMessage, NetworkType, byte[]> filterLoadPayload = (msg, networkType) -> {
        NetworkEnvelope envelope = new NetworkEnvelope(FILTERLOAD.command(), msg.serialize(), networkType);
        return envelope.serialize();
    };
     */

    public static final BiFunction<byte[], NetworkType, NetworkEnvelope> unknownMessageEnvelope = (payload, networkType) -> {
        UnknownMessage m = new UnknownMessage(payload);
        return new NetworkEnvelope(m.getMessageType().command(), m.serialize(), networkType);
    };

    public static final BiFunction<byte[], NetworkType, byte[]> unknownPayload = (payload, networkType) ->
            unknownMessageEnvelope.apply(payload, networkType).serialize();
}
