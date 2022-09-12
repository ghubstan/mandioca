package mandioca.bitcoin.parser;

import java.io.DataInputStream;
import java.nio.ByteBuffer;

import static mandioca.bitcoin.function.ByteArrayFunctions.toDataInputStream;

public class ByteBufferParser extends AbstractParser implements Parser {
    private final ByteBuffer byteBuffer;

    public ByteBufferParser(byte[] bytes) {
        try {
            this.byteBuffer = ByteBuffer.wrap(bytes);
        } catch (NullPointerException e) {
            throw new IllegalStateException("cannot create byte buffer from null byte array");
        }
    }

    @Override
    public byte read() {
        try {
            return byteBuffer.get();
        } catch (Exception e) {
            throw new RuntimeException("error reading 1 byte from " + getByteBufferInfo(), e);
        }
    }

    @Override
    public byte[] readBytes(int numBytes) {
        try {
            byte[] bytes = new byte[numBytes];
            byteBuffer.get(bytes);
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("error reading " + numBytes + " bytes from " + getByteBufferInfo(), e);
        }
    }

    // read all remaining bytes in the buffer
    @Override
    public byte[] readRemainingBytes() {
        try {
            byte[] remainingBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(remainingBytes);
            return remainingBytes;
        } catch (Exception e) {
            throw new RuntimeException("error reading remaining bytes from " + getByteBufferInfo(), e);
        }
    }

    @Override
    public long readVarint() {
        byte[] vi = new byte[]{read()}; // is big endian ordered in stream
        return readVarint(vi);  // return little endian
    }

    @Override
    public DataInputStream getInputStream() {
        byte[] bytes = new byte[byteBuffer.remaining()];  // is size 1 too big?
        byteBuffer.get(bytes);
        return toDataInputStream.apply(bytes);
    }

    @Override
    public void closeInputStream() {
        byteBuffer.clear(); // nothing to do
    }

    @Override
    public int position() {
        return byteBuffer.position();
    }

    @Override
    public int limit() {
        return byteBuffer.limit();
    }

    @Override
    public boolean hasRemaining() {
        return byteBuffer.hasRemaining();
    }

    @Override
    public void reset() {
        byteBuffer.clear();
    }

    private String getByteBufferInfo() {
        return String.format("byteBuffer with capacity = %d, hasRemaining = %s, remaining = %d, position = %d, limit = %d",
                byteBuffer.capacity(), byteBuffer.hasRemaining(), byteBuffer.remaining(), byteBuffer.position(), byteBuffer.limit());
    }
}
