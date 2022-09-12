package mandioca.bitcoin.network.message;

import java.security.SecureRandom;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import static java.lang.System.currentTimeMillis;
import static mandioca.bitcoin.function.ByteArrayFunctions.longToBytes;
import static mandioca.bitcoin.function.EndianFunctions.reverse;

public final class NonceFactory {

    // streams cannot be reused, once consumed or used, a stream will be closed, so use a supplier

    // TODO how much storage does this use?
    private final Supplier<LongStream> streamSupplier = () ->
            new SecureRandom(longToBytes.apply(currentTimeMillis())).longs(0, Long.MAX_VALUE);

    public NonceFactory() {
    }


    public synchronized byte[] getNonce() {
        long nonce = streamSupplier.get().findAny().getAsLong();
        return reverse.apply(longToBytes.apply(nonce)); // int_to_little_endian(randint(0, 2**64), 8)
    }
}
