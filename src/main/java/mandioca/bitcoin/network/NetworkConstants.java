package mandioca.bitcoin.network;

import mandioca.bitcoin.network.message.VerAckMessage;

import java.nio.ByteBuffer;

import static java.lang.System.out;
import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.util.HexUtils.HEX;


public class NetworkConstants {

    // 32 MB is largest acceptable msg size, anything bigger should be ignored
    public static final int MAX_MESSAGE_SIZE = 1024 * 1024 * 32;

    public static final int MAX_BLOCK_HEADER_BATCH_SIZE = 2000;

    public static final int BLOCK_HEADER_LENGTH = 80;

    public static final int HASH_LENGTH = 32;

    public static final byte[] ZERO_HASH = emptyArray.apply(HASH_LENGTH);

    public static final int MAGIC_LENGTH = Integer.BYTES;

    public static final int PAYLOAD_CHECKSUM_LENGTH = Integer.BYTES;

    public static final int VERSION_LENGTH = Integer.BYTES;

    public static final int COMMAND_LENGTH = 12;

    public static final int SERVICES_LENGTH = Long.BYTES;

    public static final int TIMESTAMP_LENGTH = Long.BYTES;

    public static final int ADDRESS_LENGTH = 16;

    public static final int PORT_LENGTH = 2;

    public static final int NONCE_LENGTH = Long.BYTES;

    public static final int PING_LENGTH = 32;

    public static final int MAX_VARINT_LENGTH = Long.BYTES;

    public static final int START_HEIGHT_LENGTH = Integer.BYTES;

    public static final byte[] EMPTY_ARRAY_CHECKSUM = new byte[]{(byte) 0x5d, (byte) 0xf6, (byte) 0xe0, (byte) 0xe2};

    public static final byte[] SERIALIZED_EMPTY_PAYLOAD = concatenate.apply(emptyArray.apply(Integer.BYTES), EMPTY_ARRAY_CHECKSUM);

    public static final VerAckMessage VERACK_MESSAGE = new VerAckMessage();

    public static final byte[] SEGWIT_MARKER = new byte[]{0x00, 0x01};

    private static void printEmptyArrayChecksum() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        for (int i = 0; i < 10; i++) {
            buffer.clear();
            buffer.put(hash256.apply(emptyArray.apply(0)), 0, PAYLOAD_CHECKSUM_LENGTH);
            byte[] checksum = buffer.array();
            out.println("checksum(empty-payload) = " + HEX.encode(checksum));
        }
    }

    public static void main(String[] args) {
        printEmptyArrayChecksum();
    }
}
