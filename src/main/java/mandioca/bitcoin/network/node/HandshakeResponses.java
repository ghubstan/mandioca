package mandioca.bitcoin.network.node;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.pbbl.ByteBufferLender.borrowBuffer;
import static mandioca.bitcoin.pbbl.ByteBufferLender.returnBuffer;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

public final class HandshakeResponses {

    private static final byte[] HANDSHAKE_MARKER = stringToBytes.apply("handshake");

    // TODO Find more elegant soln to the kludge, but for now this tells the SubReactor it needs to send the
    //  version and verack response in one payload.
    // 68 61 6e 64 73 68 61 6b 65 = "handshake"
    public static final byte[] MARKER = new byte[]{(byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe,
            (byte) 0x68, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x73, (byte) 0x68, (byte) 0x61, (byte) 0x6b, (byte) 0x65};

    public static final Predicate<byte[]> isHandshakeResponse = (bytes) -> bytes.length > 150
            && bytes[0] == MARKER[0]    // 251
            && bytes[1] == MARKER[1]    // 252
            && bytes[2] == MARKER[2]    // 253
            && bytes[3] == MARKER[3]    // 254
            && bytes[4] == HANDSHAKE_MARKER[0]      // 'h'
            && bytes[5] == HANDSHAKE_MARKER[1]      // 'a'
            && bytes[6] == HANDSHAKE_MARKER[2]      // 'n'
            && bytes[7] == HANDSHAKE_MARKER[3]      // 'd'
            && bytes[8] == HANDSHAKE_MARKER[4]      // 's'
            && bytes[9] == HANDSHAKE_MARKER[5]      // 'h'
            && bytes[10] == HANDSHAKE_MARKER[6]     // 'a'
            && bytes[11] == HANDSHAKE_MARKER[7]     // 'k'
            && bytes[12] == HANDSHAKE_MARKER[8];    // 'e'

    private final byte[] marker = MARKER;
    /**
     * serialized version envelope
     */
    private final byte[] versionPayload;
    /**
     * serialized verack envelope
     */
    private final byte[] verackPayload;


    public HandshakeResponses(byte[] versionPayload, byte[] verackPayload) {
        this.versionPayload = versionPayload;       // serialized version envelope
        this.verackPayload = verackPayload;         // serialized verack envelope
    }

    public byte[] getVerackPayload() {
        return verackPayload;
    }

    public byte[] getVersionPayload() {
        return versionPayload;
    }

    public static HandshakeResponses parse(byte[] bytes) {
        ByteBuffer byteBuffer = borrowBuffer.apply(bytes.length).clear();
        try {
            byte[] marker = new byte[MARKER.length];
            byteBuffer.get(marker);
            byte[] versionLength = new byte[1];
            byteBuffer.get(versionLength);
            byte[] versionPayload = new byte[bytesToInt.apply(versionLength)];
            byteBuffer.get(versionPayload);
            byte[] verackLength = new byte[1];
            byteBuffer.get(verackLength);
            byte[] verackPayload = new byte[bytesToInt.apply(verackLength)];
            byteBuffer.get(verackPayload);
            return new HandshakeResponses(versionPayload, verackPayload);
        } finally {
            returnBuffer.accept(byteBuffer.clear());
        }
    }

    public byte[] serialize() {
        // concatenate two serialized envelopes in one byte buffer for socket write op
        ByteBuffer byteBuffer = borrowBuffer.apply(versionPayload.length + verackPayload.length).clear();
        try {
            byteBuffer.put(versionPayload);
            byteBuffer.put(verackPayload);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer.clear());
        }
    }

    byte[] serializeInternal() {
        // concatenate two varint separated, serialized envelopes in one byte buffer for internal use
        byte[] versionLength = VARINT.encode(versionPayload.length);
        byte[] verackLength = VARINT.encode(verackPayload.length);
        ByteBuffer byteBuffer = borrowBuffer.apply(marker.length +
                versionLength.length + versionPayload.length +
                verackLength.length + verackPayload.length).clear();
        try {
            byteBuffer.put(marker);
            byteBuffer.put(versionLength);
            byteBuffer.put(versionPayload);
            byteBuffer.put(verackLength);
            byteBuffer.put(verackPayload);
            byteBuffer.flip();
            return byteBuffer.array();
        } finally {
            returnBuffer.accept(byteBuffer.clear());
        }
    }

    @Override
    public String toString() {
        return "HandshakeResponses{" +
                "marker=" + HEX.encode(marker) +
                ", versionPayload=" + HEX.encode(versionPayload) +
                ", verackPayload=" + HEX.encode(verackPayload) +
                '}';
    }

}
