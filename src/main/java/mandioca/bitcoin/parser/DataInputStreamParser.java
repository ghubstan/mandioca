package mandioca.bitcoin.parser;

import java.io.DataInputStream;
import java.io.IOException;

public class DataInputStreamParser extends AbstractParser implements Parser {
    private final DataInputStream is;

    public DataInputStreamParser(DataInputStream is) {
        this.is = is;
        if (is == null) {
            throw new IllegalStateException("Cannot read a null input stream");
        }
    }

    @Override
    public byte read() {
        try {
            if (is.available() == 0) {
                throw new RuntimeException("Attempt to read consumed DataInputStream; available() = 0");
            }
            return (byte) is.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readBytes(int numBytes) {
        try {
            if (is.available() == 0) {
                throw new RuntimeException("Attempt to read consumed DataInputStream; available() = 0");
            }
            byte[] buffer = new byte[numBytes];
            is.readFully(buffer);
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + numBytes + " bytes from input stream", e);
        }
    }

    // read all remaining bytes in input stream blocks until all remaining bytes
    // have been read and end of stream is detected, or an exception is thrown
    @Override
    public byte[] readRemainingBytes() {
        try {
            if (is.available() == 0) {
                throw new RuntimeException("Attempt to read consumed DataInputStream; available() = 0");
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readVarint() {
        try {
            if (is.available() == 0) {
                throw new RuntimeException("Attempt to read consumed DataInputStream; available() = 0");
            }
            byte[] vi = new byte[1]; // is big endian ordered in stream
            is.readFully(vi);
            return readVarint(vi);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DataInputStream getInputStream() {
        return is;
    }

    public void closeInputStream() {
        try {
            is.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing input stream", e);
        }
    }

    @Override
    public int position() {
        throw new RuntimeException("InputStream does not implement position()");
    }

    @Override
    public int limit() {
        throw new RuntimeException("InputStream does not implement limit()");
    }

    @Override
    public boolean hasRemaining() {
        try {
            return is.available() > 0;
        } catch (IOException e) {
            // TODO handle error better than just returning false
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void reset() {
        try {
            is.reset();
        } catch (IOException e) {
            throw new RuntimeException("error resetting input stream", e);
        }
    }

    // TODO
    /*
      private String getByteBufferInfo() {
        return String.format("byteBuffer with capacity = %d, hasRemaining = %s, remaining = %d, position = %d, limit = %d",
                byteBuffer.capacity(), byteBuffer.hasRemaining(), byteBuffer.remaining(), byteBuffer.position(), byteBuffer.limit());
    }
     */
}
