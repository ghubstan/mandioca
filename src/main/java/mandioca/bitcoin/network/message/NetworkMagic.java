package mandioca.bitcoin.network.message;

import mandioca.bitcoin.network.NetworkType;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import static mandioca.bitcoin.util.HexUtils.HEX;


// See https://en.bitcoin.it/wiki/Protocol_documentation
// See https://learnmeabitcoin.com/guide/magic-bytes

@SuppressWarnings("unused")
public enum NetworkMagic {

    NETWORK_MAGIC(new byte[]{(byte) 0xf9, (byte) 0xbe, (byte) 0xb4, (byte) 0xd9}, "First 4 bytes of a mainnet message -- 0xf9beb4d9"),
    TESTNET3_NETWORK_MAGIC(new byte[]{(byte) 0x0b, (byte) 0x11, (byte) 0x09, (byte) 0x07}, "First 4 bytes of a testnet3 message -- 0x0b110907"),
    REGTEST_NETWORK_MAGIC(new byte[]{(byte) 0xfa, (byte) 0xbf, (byte) 0xb5, (byte) 0xda}, "First 4 bytes of a regtest message -- 0xfabfb5da");

    public static final Predicate<NetworkMagic> isMainnet = (n) -> n.equals(NETWORK_MAGIC);
    public static final Predicate<NetworkMagic> isTestnet = (n) -> n.equals(TESTNET3_NETWORK_MAGIC);
    public static final Predicate<NetworkMagic> isRegtest = (n) -> n.equals(REGTEST_NETWORK_MAGIC);

    public static final Function<byte[], NetworkMagic> bytesToNetworkMagic = (bytes) -> {
        if (Arrays.equals(bytes, TESTNET3_NETWORK_MAGIC.bytes)) {
            return TESTNET3_NETWORK_MAGIC;
        } else if (Arrays.equals(bytes, NETWORK_MAGIC.bytes)) {
            return NETWORK_MAGIC;
        } else if (Arrays.equals(bytes, REGTEST_NETWORK_MAGIC.bytes)) {
            return REGTEST_NETWORK_MAGIC;
        } else {
            throw new IllegalStateException("Unknown NetworkMagic bytes " + HEX.encode(bytes));
        }
    };

    public static final Function<NetworkType, NetworkMagic> networkTypeToMagic = (t) -> {
        if (NetworkType.isTestnet.test(t)) {
            return NetworkMagic.TESTNET3_NETWORK_MAGIC;
        } else if (NetworkType.isMainnet.test(t)) {
            return NetworkMagic.NETWORK_MAGIC;
        } else if (NetworkType.isRegtest.test(t)) {
            return NetworkMagic.REGTEST_NETWORK_MAGIC;
        } else {
            throw new IllegalStateException("Unknown NetworkType " + t);
        }
    };

    final byte[] bytes;
    final String description;

    NetworkMagic(byte[] bytes, String description) {
        this.bytes = bytes;
        this.description = description;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public String getHex() {
        return HEX.encode(this.bytes);
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "NetworkMagic{" +
                "bytes=" + Arrays.toString(bytes) +
                "hex=" + getHex() +
                ", description='" + description + '\'' +
                '}';
    }
}
