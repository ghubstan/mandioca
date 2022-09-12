package mandioca.bitcoin.util;

import org.junit.Test;

import java.math.BigInteger;

import static mandioca.bitcoin.function.BigIntegerFunctions.HEX_RADIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.bigIntToUnsignedByteArray;
import static org.junit.Assert.assertEquals;

public class Base58Test {

    // Convert hex to binary to base 58

    @Test // Test data from Jimmy Song book, Chapter 4, Exercise 4
    public void test1() {
        BigInteger hexInt = new BigInteger("7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d", HEX_RADIX);
        byte[] bytes = bigIntToUnsignedByteArray.apply(hexInt);
        String base58 = Base58.encode(bytes);
        String expectedBase58 = "9MA8fRQrT4u8Zj8ZRd6MAiiyaxb2Y1CMpvVkHQu5hVM6";
        assertEquals(expectedBase58, base58);
    }

    @Test // Test data from Jimmy Song book, Chapter 4, Exercise 4
    public void test2() {
        BigInteger hexInt = new BigInteger("eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c", HEX_RADIX);
        byte[] bytes = bigIntToUnsignedByteArray.apply(hexInt);
        String base58 = Base58.encode(bytes);
        // Note: book is missing the leading 1 (typo)
        String expectedBase58 = "1" + "4fE3H2E6XMp4SsxtwinF7w9a34ooUrwWe4WsW1458Pd";
        assertEquals(expectedBase58, base58);
    }

    @Test // Test data from Jimmy Song book, Chapter 4, Exercise 4
    public void test3() {
        BigInteger hexInt = new BigInteger("c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6", HEX_RADIX);
        byte[] bytes = bigIntToUnsignedByteArray.apply(hexInt);
        String base58 = Base58.encode(bytes);
        String expectedBase58 = "EQJsjkd6JaGwxrjEhfeqPenqHwrBmPQZjJGNSCHBkcF7";
        assertEquals(expectedBase58, base58);
    }
}
