package mandioca.bitcoin.address;

import mandioca.bitcoin.ecc.Secp256k1Point;
import mandioca.bitcoin.function.ThrowingFunction;
import mandioca.bitcoin.function.TriFunction;
import mandioca.bitcoin.network.NetworkType;

import java.io.ByteArrayOutputStream;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.copyOfRange;
import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.HashFunctions.hash256;
import static mandioca.bitcoin.network.NetworkType.p2pkhNetworkPrefix;
import static mandioca.bitcoin.network.NetworkType.p2shNetPrefix;
import static mandioca.bitcoin.util.Base58.*;

// See https://en.bitcoin.it/wiki/Technical_background_of_version_1_Bitcoin_addresses
// See https://github.com/nayuki/Bitcoin-Cryptography-Library/tree/master/java/io/nayuki/bitcoin/crypto
// See https://allprivatekeys.com/bitcoin-address-format
// See https://bitcoin.stackexchange.com/questions/78861/testnet-bitcoin-wallet-which-script-in-hash-addresses
// See https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/LegacyAddress.java
// See https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/Address.java

@SuppressWarnings("unused")
public class AddressFactory {

    public static final Function<String, byte[]> legacyAddressToHash = (a) -> decodeChecked(a, true);

    private static ThrowingFunction<byte[], Boolean> validateLegacyAddressSecHash = (hash160) -> {
        if (hash160.length != 20) {
            throw new AddressFormatException.InvalidDataLength(
                    "Legacy addresses are 20 byte (160 bit) hashes, but got: " + hash160.length + " bytes");
        }
        return true;
    };

    public static final BiFunction<byte[], NetworkType, Address> scriptHashToP2pkh = (hash, n) -> {
        validateLegacyAddressSecHash.apply(hash);
        byte[] prefix = p2pkhNetworkPrefix.apply(n);
        String address = encodeChecked(concatenate.apply(prefix, hash));
        return new Pay2PubKeyHashAddress(n, address);
    };


    public static final BiFunction<byte[], NetworkType, Address> scriptHashToP2sh = (hash, n) -> {
        validateLegacyAddressSecHash.apply(hash);
        byte[] prefix = p2shNetPrefix.apply(n);
        String address = encodeChecked(concatenate.apply(prefix, hash));
        return new Pay2ScriptHashAddress(n, address);
    };

    public static final TriFunction<Secp256k1Point, Boolean, NetworkType, Address> publicKeyToP2pkhAddress = (p, c, n) -> {
        final String addr = getAddress(p, p2pkhNetworkPrefix.apply(n), c);
        return new Pay2PubKeyHashAddress(n, addr);
    };

    public static final TriFunction<Secp256k1Point, Boolean, NetworkType, Address> publicKeyToP2pshAddress = (p, c, n) -> {
        final String addr = getAddress(p, p2shNetPrefix.apply(n), c);
        return new Pay2ScriptHashAddress(n, addr);
    };

    public static final Function<NetworkType, Address> nullDataAddress = (n) -> new NullDataAddress(n, "no address");


    private static String getAddress(Secp256k1Point publicKey, byte[] networkPrefix, boolean compressed) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(networkPrefix);                                      // step 1:  define addr prefix
            byte[] hash160 = publicKey.secHash160.apply(compressed);        // step 2:  hash160(hash of sec)
            validateLegacyAddressSecHash.apply(hash160);
            baos.write(hash160);            // step 3:  append hash160 to prefix to get the payload to be base58 encoded
            byte[] payload = baos.toByteArray();    // step 4:  take 1st 4 bytes of dbl-hashed payload to get checksum
            byte[] checksum = copyOfRange(hash256.apply(payload), 0, 4);
            baos.write(checksum);           // step 5:  append the checksum to the payload and encode it
            return encode(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error deriving address from public key:\n" + publicKey, e);
        }
    }
}
