package mandioca.bitcoin.network.block.io;

import mandioca.bitcoin.network.node.NetworkEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

import static mandioca.bitcoin.network.NetworkConstants.*;

public final class BlockHeaderReader extends BlockHeaderIO implements Callable<NetworkEnvelope> {

    private static final Logger log = LoggerFactory.getLogger(BlockHeaderReader.class);

    // The buffer must be large enough to read a batch of 2000 block headers @ 80 bytes each, plus the 2,4 or 8
    // byte varint following each header;  the num tx value should always be a 1 byte 0x00, but make room for 8.
    private static final int READ_BUFFER_SIZE = MAX_BLOCK_HEADER_BATCH_SIZE * (BLOCK_HEADER_LENGTH + MAX_VARINT_LENGTH);

    public BlockHeaderReader(SocketChannel socketChannel) {
        super(socketChannel);
    }

    @Override
    public NetworkEnvelope call() throws Exception {
        return getEnvelope(READ_BUFFER_SIZE);
    }

}
