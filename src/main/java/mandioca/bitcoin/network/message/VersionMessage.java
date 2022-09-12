package mandioca.bitcoin.network.message;

// See https://en.bitcoin.it/wiki/Version_Handshake
// See https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki
// See https://github.com/bitcoin/bips/blob/master/bip-0014.mediawiki

import mandioca.bitcoin.network.NetworkType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.NetworkConstants.SERVICES_LENGTH;
import static mandioca.bitcoin.network.NetworkConstants.START_HEIGHT_LENGTH;
import static mandioca.bitcoin.network.NetworkProperties.*;
import static mandioca.bitcoin.network.NetworkType.MAINNET;
import static mandioca.bitcoin.network.message.MessageType.VERSION;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

/**
 * Version Handshake
 * <p>
 * When the local peer (L) connects to a remote peer (R), the remote peer will not send any data until it receives
 * a version message.
 * <p>
 * L -> R: Send version message with the local peer's version
 * R -> L: Send version message back
 * R -> L: Send verack message
 * R:      Sets version to the minimum of the 2 versions
 * L -> R: Send verack message after receiving version message from R
 * L:      Sets version to the minimum of the 2 versions
 * <p>
 * Note: Versions below 31800 are no longer supported.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Version_Handshake">https://en.bitcoin.it/wiki/Version_Handshake</a>
 * @see <a href="https://bitcoindev.network/bitcoin-wire-protocol">https://bitcoindev.network/bitcoin-wire-protocol</a>
 */
@SuppressWarnings("unused")
public final class VersionMessage extends AbstractNetworkMessage implements NetworkMessage {

    public static final byte[] DEFAULT_VERSION = reverse.apply(intToBytes.apply(NETWORK_PROTOCOL_VERSION));

    static final byte[] DEFAULT_ADDRESS = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x00, 0x00};
    static final byte[] DEFAULT_SERVICES = new byte[]{0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    static final byte[] DEFAULT_USER_AGENT = stringToBytes.apply(DEFAULT_USERAGENT);
    static final byte[] DEFAULT_LATEST_BLOCK = new byte[]{0x00};
    static final byte[] DEFAULT_RELAY = new byte[]{0x00};

    static final Function<Integer, byte[]> serializePort = intToTruncatedByteArray;
    public static final Function<NetworkType, byte[]> serializedVersionPort = (n) -> {
        switch (n) {
            // return two byte array
            case MAINNET:
                return serializePort.apply(MAINNET_PORT);
            case TESTNET3:
                return serializePort.apply(TESTNET3_PORT);
            case REGTEST:
                return serializePort.apply(REGTEST_PORT);
            default:
                throw new RuntimeException("cannot determine truncated version port for " + n);
        }
    };

    private final byte[] version;           // 4 bytes, little endian identifier of protocol version being used by the node
    private final byte[] services;          // 8 bytes, little endian bitfield of features enabled for this connection
    private final byte[] timestamp;         // 8 bytes, little endian standard UNIX timestamp in seconds
    private final byte[] receiverServices;  // 8 bytes, little endian bitfield of receiver's features enabled for connection
    private final byte[] receiverAddress;   // 16 bytes, big endian IP address -- IPv4 / IPv6 / OnionCat
    private final byte[] receiverPort;      // 2 bytes 8333 (mainnet) 18333 (testnet3)
    private final byte[] senderServices;    // 8 bytes, little endian bitfield of sender's features enabled for connection
    private final byte[] senderAddress;     // 16 bytes, big endian IP address -- IPv4 / IPv6 / OnionCat
    private final byte[] senderPort;        // 2 bytes 8333 (mainnet) 18333 (testnet3)
    private final byte[] nonce;             // 8 bytes, little endian value randomly generated every time a version packet is sent; is used to detect connections to self
    private final byte[] userAgent;         // variable len, big endian (?) software identifier ('0x00' if string is 0 bytes long)
    private final byte[] startHeight;       // 4 bytes, little endian(?), last block received by the emitting node (which block a node is sync'd to)
    private final byte[] relay;             // 1 byte indicating if remote peer should announce relayed transactions or not, see BIP 0037
    //                                          (set relay = 0 to tell node not to send transaction data unless matches bloom filter)

    private VersionMessage(byte[] version,
                           byte[] services,
                           byte[] timestamp,
                           byte[] receiverServices,
                           byte[] receiverAddress,
                           byte[] receiverPort,
                           byte[] senderServices,
                           byte[] senderAddress,
                           byte[] senderPort,
                           byte[] nonce,
                           byte[] userAgent,
                           byte[] startHeight,
                           byte[] relay) {
        // Hidden constructor containing too many arguments of same type to overload.  Use the builder instead.
        this.messageType = VERSION;
        this.version = version;
        this.services = services;
        this.timestamp = (timestamp == null) ? serializedLongTimestamp.get() : timestamp;
        this.receiverServices = receiverServices;
        this.receiverAddress = receiverAddress;
        this.receiverPort = receiverPort;
        this.senderServices = senderServices;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.nonce = (nonce == null) ? getNewNonce() : nonce;
        this.userAgent = userAgent;
        this.startHeight = startHeight;
        this.relay = relay;
    }

    public VersionMessage(VersionMessageBuilder versionMessageBuilder) {
        this.messageType = VERSION;
        this.version = (versionMessageBuilder.version == null) ? DEFAULT_VERSION : versionMessageBuilder.version;
        this.services = (versionMessageBuilder.services == null) ? emptyArray.apply(SERVICES_LENGTH) : versionMessageBuilder.services;
        this.timestamp = (versionMessageBuilder.timestamp == null) ? serializedLongTimestamp.get() : versionMessageBuilder.timestamp;
        this.receiverServices = (versionMessageBuilder.receiverServices == null) ? DEFAULT_SERVICES : versionMessageBuilder.receiverServices;
        this.receiverAddress = (versionMessageBuilder.receiverAddress == null) ? DEFAULT_ADDRESS : versionMessageBuilder.receiverAddress;
        this.receiverPort = (versionMessageBuilder.receiverPort == null) ? serializedVersionPort.apply(MAINNET) : versionMessageBuilder.receiverPort;
        this.senderServices = (versionMessageBuilder.senderServices == null) ? DEFAULT_SERVICES : versionMessageBuilder.senderServices;
        this.senderAddress = (versionMessageBuilder.senderAddress == null) ? DEFAULT_ADDRESS : versionMessageBuilder.senderAddress;
        this.senderPort = (versionMessageBuilder.senderPort == null) ? serializedVersionPort.apply(MAINNET) : versionMessageBuilder.senderPort;
        this.nonce = (versionMessageBuilder.nonce == null) ? getNewNonce() : versionMessageBuilder.nonce;
        this.userAgent = (versionMessageBuilder.userAgent == null) ? DEFAULT_USER_AGENT : versionMessageBuilder.userAgent;
        this.startHeight = (versionMessageBuilder.startHeight == null) ? emptyArray.apply(START_HEIGHT_LENGTH) : versionMessageBuilder.startHeight;
        this.relay = (versionMessageBuilder.relay == null) ? DEFAULT_RELAY : versionMessageBuilder.relay;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(version);
            baos.write(services);
            baos.write(timestamp);
            baos.write(receiverServices);
            baos.write(receiverAddress);
            baos.write(receiverPort);
            baos.write(senderServices);
            baos.write(senderAddress);
            baos.write(senderPort);
            baos.write(nonce);
            baos.write(VARINT.encode(userAgent.length));
            baos.write(userAgent);
            baos.write(startHeight);
            baos.write(relay);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing version message", e);
        }
    }

    /**
     * VersionMessage builder helps avoid bungling use of the VersionMessage constructor
     * argument list, which is large, of all one type, and not wise to overload.
     */
    public static class VersionMessageBuilder {
        private byte[] version;           // 4 bytes, little endian identifier of protocol version being used by the node
        private byte[] services;          // 8 bytes, little endian bitfield of features enabled for this connection
        private byte[] timestamp;         // 8 bytes, little endian standard UNIX timestamp in seconds
        private byte[] receiverServices;  // 8 bytes, little endian bitfield of receiver's features enabled for connection
        private byte[] receiverAddress;   // 16 bytes, big endian IP address -- IPv4 / IPv6 / OnionCat
        private byte[] receiverPort;      // 2 bytes 8333 (mainnet) 18333 (testnet3)
        private byte[] senderServices;    // 8 bytes, little endian bitfield of sender's features enabled for connection
        private byte[] senderAddress;     // 16 bytes, big endian IP address -- IPv4 / IPv6 / OnionCat
        private byte[] senderPort;        // 2 bytes 8333 (mainnet) 18333 (testnet3)
        private byte[] nonce;             // 8 bytes, little endian value randomly generated every time a version packet is sent; is used to detect connections to self
        private byte[] userAgent;         // variable len, big endian (?) software identifier ('0x00' if string is 0 bytes long)
        private byte[] startHeight;       // 4 bytes, little endian(?), last block received by the emitting node (which block a node is sync'd to)
        private byte[] relay;             // 1 byte indicating if remote peer should announce relayed transactions or not, see BIP 0037

        public VersionMessageBuilder() {
        }

        public VersionMessageBuilder withVersion(byte[] version) {
            this.version = version;
            return this;
        }

        public VersionMessageBuilder withServices(byte[] services) {
            this.services = services;
            return this;
        }

        public VersionMessageBuilder withTimestamp(byte[] timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public VersionMessageBuilder withTimestamp(long timestamp) {
            this.timestamp = serializeLongTimestamp.apply(timestamp);
            return this;
        }

        public VersionMessageBuilder withTimestamp() {
            this.timestamp = serializedLongTimestamp.get();
            return this;
        }

        public VersionMessageBuilder withReceiverServices(byte[] receiverServices) {
            this.receiverServices = receiverServices;
            return this;
        }

        public VersionMessageBuilder withReceiverAddress(byte[] receiverAddress) {
            this.receiverAddress = receiverAddress;
            return this;
        }

        public VersionMessageBuilder withReceiverPort(byte[] receiverPort) {
            this.receiverPort = receiverPort;
            return this;
        }

        public VersionMessageBuilder withSenderServices(byte[] senderServices) {
            this.senderServices = senderServices;
            return this;
        }

        public VersionMessageBuilder withSenderAddress(byte[] senderAddress) {
            this.senderAddress = senderAddress;
            return this;
        }

        public VersionMessageBuilder withSenderPort(byte[] senderPort) {
            this.senderPort = senderPort;
            return this;
        }

        public VersionMessageBuilder withNonce(byte[] nonce) {
            this.nonce = nonce;
            return this;
        }

        public VersionMessageBuilder withUserAgent(byte[] userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public VersionMessageBuilder withStartHeight(byte[] startHeight) {
            this.startHeight = startHeight;
            return this;
        }

        public VersionMessageBuilder withRelay(byte[] relay) {
            this.relay = relay;
            return this;
        }

        public VersionMessage build() {
            return new VersionMessage(this);
        }
    }
}
