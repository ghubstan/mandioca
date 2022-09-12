package mandioca.bitcoin.function;

import java.util.BitSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BitSetFunctions {

    /**
     * Returns a string containing decimal representations for each set bit's index.
     */
    public static final Function<byte[], String> setBitsToString = (bytes) -> BitSet.valueOf(bytes).toString();

    /**
     * Returns true if all bits for in the given array for the given indexes are set, false otherwise
     */
    public static final BiFunction<byte[], Integer[], Boolean> bitsAreSet = (bytes, indexes) -> {
        BitSet bitSet = BitSet.valueOf(bytes);
        for (Integer index : indexes) {
            if (!bitSet.get(index)) {
                return false;
            }
        }
        return true;
    };
}
