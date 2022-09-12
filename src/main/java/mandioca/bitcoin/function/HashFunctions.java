package mandioca.bitcoin.function;

import mandioca.bitcoin.util.Ripemd160;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;
import java.util.function.Supplier;

public class HashFunctions {

    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";

    public static final Supplier<MessageDigest> newSHA1Digest = () -> {
        try {
            return MessageDigest.getInstance(SHA1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    };

    public static final Function<byte[], byte[]> sha1Hash = (data) -> {
        MessageDigest digest = newSHA1Digest.get();
        return digest.digest(data);
    };

    public static final Supplier<MessageDigest> newSHA256Digest = () -> {
        try {
            return MessageDigest.getInstance(SHA256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    };

    public static final Function<byte[], byte[]> sha256Hash = (data) -> {
        MessageDigest digest = newSHA256Digest.get();
        return digest.digest(data);
    };

    public static final Function<byte[], byte[]> hash256 = (data) -> {
        // a double sha256 hash
        MessageDigest digest = newSHA256Digest.get();
        byte[] hash = digest.digest(data); // 1st hash
        return digest.digest(hash); // 2nd hash
    };

    public static final TriFunction<byte[], Integer, Integer, byte[]> hash256FromOffset = (data, offset, len) -> {
        MessageDigest digest = newSHA256Digest.get();
        digest.update(data, offset, len);
        byte[] hash = digest.digest(data);          // 1st hash
        return digest.digest(hash);    // 2nd hash
    };

    public static final Function<byte[], byte[]> hashRipemd160 = Ripemd160::getHash;

    public static final Function<byte[], byte[]> hash160 = (data) -> hashRipemd160.apply(sha256Hash.apply(data));

}
