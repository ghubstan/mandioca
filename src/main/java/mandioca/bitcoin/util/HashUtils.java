package mandioca.bitcoin.util;

import java.math.BigInteger;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.stringToBytes;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.function.HashFunctions.sha256Hash;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class HashUtils {

    public static String getSHA256HashAsString(String data) {
        byte[] hash = sha256Hash.apply(stringToBytes.apply(data));
        return HEX.to64DigitPaddedHex(hash);
    }

    public static BigInteger getSHA256HashAsInteger(String data) {
        return new BigInteger(getSHA256HashAsString(data), HEX_RADIX);
    }

    public static String getSHA256DoubleHashAsString(String data) {
        return HEX.to64DigitPaddedHex(hash256.apply(stringToBytes.apply(data)));
    }

    public static BigInteger getDoubleSHA256HashAsInteger(String data) {
        return new BigInteger(getSHA256DoubleHashAsString(data), HEX_RADIX);
    }
}
