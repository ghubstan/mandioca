package mandioca.bitcoin.pbbl;

import mandioca.bitcoin.pbbl.heap.HeapByteBufferPool;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ByteBufferLender {
    private static final HeapByteBufferPool byteBufferPool = new HeapByteBufferPool();  // MIT license
    public static final Function<Integer, ByteBuffer> borrowBuffer = byteBufferPool::takeExact;
    public static final Consumer<ByteBuffer> returnBuffer = byteBufferPool::give;
}
