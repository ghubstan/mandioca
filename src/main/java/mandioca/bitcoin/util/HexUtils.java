package mandioca.bitcoin.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;

@SuppressWarnings("ALL")
public class HexUtils {

    public static final HexUtils HEX = new HexUtils();

    public static final String THIRTY_TWO_ZEROS = new String(new char[32]).replace("\0", "0");
    public static final String SIXTY_FOUR_ZEROS = new String(new char[64]).replace("\0", "0");

    public final Function<String, String> cleanOctets = (s) -> s.replaceAll("[\\s+:]", "").toUpperCase();
    public final Function<String, String> toOctet = (hex) -> (hex.length() == 1) ? '0' + hex : hex;
    public final Function<Byte, String> toPositiveHex = (b) -> Integer.toHexString(b & MASK_0xFF);
    public final Function<Byte, String> toPaddedHex = (b) -> toOctet.apply(toPositiveHex.apply(b)); // TODO duplicates byteToPrefixedHex
    public final Predicate<String> has0xPrefix = (s) -> s.startsWith("0x");
    public final UnaryOperator<String> strip0xPrefix = (s) -> {
        if (has0xPrefix.test(s)) {
            return s.substring("0x".length());
        } else {
            return s;
        }
    };
    public final Predicate<String> isOddLength = (hex) -> hex.length() % 2 != 0;
    public final Function<String, BigInteger> stringToBigInt = (s) -> new BigInteger(strip0xPrefix.apply(s), HEX_RADIX);
    public final Function<BigInteger, String> bigIntToHex = (i) -> i.toString(HEX_RADIX);
    public final Function<Byte, String> byteToHex = (b) -> String.format("%02X", b).toLowerCase();
    public final Function<Byte, String> byteToPrefixedHex = (b) -> String.format("0x%02X", b).toLowerCase();

    public final byte[] decode(CharSequence hex) {
        // Modified from https://github.com/nayuki/Bitcoin-Cryptography-Library/blob/master/java/io/nayuki/bitcoin/crypto/Utils.java
        // Copyright © 2019 Project Nayuki. (MIT License)
        if (isOddLength.test(hex.toString())) {
            throw new IllegalStateException("Hex string has odd length");
        }
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            if (hex.charAt(i) == '+' || hex.charAt(i) == '-') {
                throw new IllegalStateException("Hex string contains + or - character(s)");
            }
            b[i / 2] = (byte) Integer.parseInt(hex.toString().substring(i, i + 2), HEX_RADIX);
        }
        return b;
    }

    public final String prettyOctets(String octets) {
        if (octets.isEmpty()) {
            throw new RuntimeException("Cannot transform empty octet string");
        } else {
            return cleanOctets.apply(octets);
        }
    }

    public final String encode(byte[][] byteArrays) {
        StringBuffer buf = new StringBuffer();
        Arrays.stream(byteArrays).forEach(a -> buf.append(HEX.encode(a)));
        return buf.toString();
    }

    public final String encode(byte[] bytes) {
        // Array size is not bounded
        if (bytes == null) {
            throw new IllegalStateException("Cannot transform null byte[]");
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(toOctet.apply(toPositiveHex.apply(b)));
        }
        return hexString.toString();
    }

    public String encode(BigInteger i) {
        if (i == null) {
            return null;
        }
        if (i.toByteArray().length > 33) {
            return to64DigitPaddedHex(i.toString(16));
        }
        return i.toString(16);
    }

    public String toPrefixedHexString(byte[] bytes) {
        return "0x" + encode(bytes);
    }

    public String to64DigitPaddedHex(byte[] bytes) {
        // Array size bound = 64
        if (bytes == null || bytes.length != 32) {
            throw new IllegalStateException("Incorrect byte[] size for padded hex transform");
        }
        return to64DigitPaddedHex(encode(bytes));
    }

    public String toPrettyHex(BigInteger i) {
        return "0x" + to64DigitPaddedHex(i);
    }

    public String to32DigitPaddedHex(String unpadded) {
        return THIRTY_TWO_ZEROS.substring(unpadded.length()) + unpadded;
    }

    public String to32DigitPaddedHex(BigInteger i) {
        String hex = bigIntToHex.apply(i);
        return THIRTY_TWO_ZEROS.substring(hex.length()) + hex;
    }

    public String to64DigitPaddedHex(String unpadded) {
        return SIXTY_FOUR_ZEROS.substring(unpadded.length()) + unpadded;
    }

    public String to64DigitPaddedHex(BigInteger i) {
        String hex = bigIntToHex.apply(i);
        return SIXTY_FOUR_ZEROS.substring(hex.length()) + hex;
    }
}
