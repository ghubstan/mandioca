package mandioca.bitcoin.network.node;

// See https://en.bitcoin.it/wiki/Protocol_documentation

import mandioca.bitcoin.network.NetworkType;
import mandioca.bitcoin.network.message.NetworkCommand;
import mandioca.bitcoin.network.message.NetworkMagic;

import java.io.ByteArrayInputStream;

import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.message.NetworkMagic.networkTypeToMagic;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class NetworkEnvelope {

    private static final NetworkEnvelopeSerializer serializer = new NetworkEnvelopeSerializer();
    private static final NetworkEnvelopeParser parser = new NetworkEnvelopeParser();

    private final NetworkCommand networkCommand;
    private final byte[] payload;
    private final NetworkType networkType;
    private final NetworkMagic magic;


    public NetworkEnvelope(NetworkCommand networkCommand, byte[] payload) {
        this(networkCommand, payload, NETWORK);
    }

    public NetworkEnvelope(NetworkCommand networkCommand, byte[] payload, NetworkType networkType) {
        this.networkCommand = networkCommand;
        this.payload = payload;
        this.networkType = networkType;
        this.magic = networkTypeToMagic.apply(networkType);
    }

    public ByteArrayInputStream stream() {
        return toByteArrayInputStream.apply(payload);  // stream for parsing the payload
    }

    // This is NOT threadsafe
    public static NetworkEnvelope parse(ByteArrayInputStream bais, NetworkType networkType) {
        return parser.init(bais).parse(networkType);
    }

    // This is NOT threadsafe
    public static NetworkEnvelope[] parseAll(ByteArrayInputStream bais, NetworkType networkType) {
        return parser.init(bais).parseAll(networkType);
    }

    public byte[] serialize() {
        return serializer.init(this).serialize();
    }

    public NetworkMagic getMagic() {
        return magic;
    }

    public NetworkCommand getNetworkCommand() {
        return networkCommand;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPayloadAsHex() {
        return HEX.encode(payload);
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    @Override
    public String toString() {
        return "NetworkEnvelope{" +
                "networkCommand=" + networkCommand.getAscii() + "\n" +
                ", payload.size=" + payload.length + "\n" +
                ", payload=" + getPayloadAsHex() + "\n" +
                ", networkType=" + networkType +
                '}';
    }

    public String toShortString() {
        return "NetworkEnvelope{ command=" + networkCommand.getAscii() + ", payload.size=" + payload.length + ", payload=" + getPayloadAsHex() + " }";
    }
}
