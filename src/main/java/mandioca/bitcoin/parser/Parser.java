package mandioca.bitcoin.parser;

import java.io.DataInputStream;

public interface Parser {
    byte read();

    byte[] readBytes(int bufferSize);

    byte[] readRemainingBytes();

    long readVarint();

    long getDecodedByteCount();

    void incrementDecodedByteCount(int n);

    void decrementDecodedByteCount(int n);

    DataInputStream getInputStream();

    void closeInputStream();

    int position();

    int limit();

    boolean hasRemaining();

    void reset();
}
