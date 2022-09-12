package mandioca.bitcoin.network.block.io;

import mandioca.bitcoin.network.block.BlockHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public final class BlockHeaderWriter extends BlockHeaderIO implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(BlockHeaderWriter.class);

    private final List<BlockHeader> blockHeaders;
    private final Path path;

    public BlockHeaderWriter(List<BlockHeader> blockHeaders, Path path) {
        super(null);  // TODO need a socket?
        this.blockHeaders = blockHeaders;
        this.path = path;
    }

    @Override
    public Integer call() throws Exception {
        // write to node.data.dir / blocks ?
        return 0;   // return number of block headers written to disk
    }
}
