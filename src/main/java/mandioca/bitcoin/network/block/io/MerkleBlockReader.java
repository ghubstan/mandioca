package mandioca.bitcoin.network.block.io;

import mandioca.bitcoin.network.node.NetworkEnvelope;
import mandioca.bitcoin.util.ByteArrayInOutStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static mandioca.bitcoin.network.NetworkConstants.*;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.channelInfo;

public class MerkleBlockReader extends BlockHeaderIO implements Callable<NetworkEnvelope[]> {

    private static final Logger log = LoggerFactory.getLogger(MerkleBlockReader.class);

    // Max # of merkleblock msgs returned is 2000
    // TODO calculate correct buffer size (can only estimate since tx msg are mixed in)
    private static final int READ_BUFFER_SIZE = MAX_BLOCK_HEADER_BATCH_SIZE * (BLOCK_HEADER_LENGTH + MAX_VARINT_LENGTH);

    public MerkleBlockReader(SocketChannel socketChannel) {
        super(socketChannel);
    }

    @Override
    public NetworkEnvelope[] call() throws Exception {
        return getEnvelopes(READ_BUFFER_SIZE);
    }

    protected NetworkEnvelope[] getEnvelopes(int bufferSize) throws Exception {
        ByteBuffer byteBuffer = getBuffer(bufferSize);
        try {
            List<byte[]> payloads;
            if (socketChannel.isConnected()) {
                socketChannel.configureBlocking(false);  // do this or socket.read may block forever, regardless of timeout
                payloads = new ArrayList<>(getPayloads(byteBuffer));
            } else {
                throw new RuntimeException("socket channel " + channelInfo.apply(socketChannel) + " not connected");
            }
            if (payloads.size() > 0) {
                return parseEnvelopes(payloads);
            } else {
                return new NetworkEnvelope[]{};
            }
        } finally {
            socketChannel.configureBlocking(true);
            returnBuffer(byteBuffer);
        }
    }


    protected NetworkEnvelope[] parseEnvelopes(List<byte[]> payloads) {
        // Mix of MerkleBlockMessages and Transactions (Tx).
        try (ByteArrayInOutStream baos = new ByteArrayInOutStream()) {
            for (byte[] payload : payloads) {
                baos.write(payload);
            }
            return NetworkEnvelope.parseAll(baos.getInputStream(), NETWORK);
        } catch (IOException e) {
            throw new RuntimeException("error parsing block envelope", e);
        }
    }

}
