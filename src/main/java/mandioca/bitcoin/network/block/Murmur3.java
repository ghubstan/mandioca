package mandioca.bitcoin.network.block;

/**
 * A pure Java implementation of the Murmur 3 hashing algorithm as presented
 * at <a href="https://sites.google.com/site/murmurhash/">Murmur Project</a>
 * <p>
 * Code is ported from original C++ source at
 * <a href="https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp">
 * MurmurHash3.cpp</a>
 *
 * @author sangupta
 * @since 1.0
 */
public final class Murmur3 {

    // MURMUR:
    // # Caution: Jimmy Song's Python algorithm is returning an unsigned python integer (>=0)
    // # while standard Java solutions return an 32bit signed integer (2 complement)
    // https://stackoverflow.com/questions/13305290/is-there-a-pure-python-implementation-of-murmurhash#15754791


    // 00000001, 00000010, 00000100, 00001000, ...
    private static final int[] bitMask = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};

    /**
     * Sets the given bit in data to one, using little endian (not the same as Java native big endian)
     */
    public static void setBitLE(byte[] data, int index) {
        data[index >>> 3] |= bitMask[7 & index];
    }

    /**
     * The Bitcoinj implementation, which looks almost identical to the python version Jimmy Song's book uses @
     * https://stackoverflow.com/questions/13305290/is-there-a-pure-python-implementation-of-murmurhash#15754791
     * <p>
     * The with the comment:
     * "Caution: The algorithm is returning an unsigned python integer (>=0) while standard Java solutions return an
     * 32bit signed integer (2 complement) – nob Jan 16 at 9:57
     *
     * <p>
     * Applies the MurmurHash3 (x86_32) algorithm to the given data.
     * See this <a href="https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp">C++ code for the original.</a>
     */
    public static int murmurHash3(byte[] data, long nTweak, int hashNum, byte[] object) {
        int h1 = (int) (hashNum * 0xFBA4C795L + nTweak);
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int numBlocks = (object.length / 4) * 4;
        // body
        for (int i = 0; i < numBlocks; i += 4) {
            int k1 = (object[i] & 0xFF) |
                    ((object[i + 1] & 0xFF) << 8) |
                    ((object[i + 2] & 0xFF) << 16) |
                    ((object[i + 3] & 0xFF) << 24);

            k1 *= c1;
            k1 = rotateLeft32(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = rotateLeft32(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        int k1 = 0;
        switch (object.length & 3) {
            case 3:
                k1 ^= (object[numBlocks + 2] & 0xff) << 16;
                // Fall through.
            case 2:
                k1 ^= (object[numBlocks + 1] & 0xff) << 8;
                // Fall through.
            case 1:
                k1 ^= (object[numBlocks] & 0xff);
                k1 *= c1;
                k1 = rotateLeft32(k1, 15);
                k1 *= c2;
                h1 ^= k1;
                // Fall through.
            default:
                // Do nothing.
                break;
        }

        // finalization
        h1 ^= object.length;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return (int) ((h1 & 0xFFFFFFFFL) % (data.length * 8));
    }

    private static int rotateLeft32(int x, int r) {
        return (x << r) | (x >>> (32 - r));
    }

}

