package mandioca.bitcoin.network;

import java.util.function.Function;
import java.util.function.Predicate;

import static mandioca.bitcoin.address.AddressConstants.*;
import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;
import static mandioca.bitcoin.util.HexUtils.HEX;

@SuppressWarnings("unused")
public enum NetworkType {

    MAINNET(P2PKH_ADDR_NET_PREFIX, P2SH_ADDR_NET_PREFIX, "mainnet"),
    REGTEST(new byte[]{(byte) MASK_0xFF}, new byte[]{(byte) MASK_0xFF}, "regtest"),
    TESTNET3(P2PKH_ADDR_TESTNET_PREFIX, P2SH_ADDR_TESTNET_PREFIX, "testnet");

    final byte[] p2pkhPrefix;
    // todo final byte[] p2wpkhPrefix;
    final byte[] p2shPrefix;
    // todo final byte[] p2wshPrefix;

    final String description;

    NetworkType(byte[] p2pkhPrefix, byte[] p2shPrefix, String description) {
        this.p2pkhPrefix = p2pkhPrefix;
        this.p2shPrefix = p2shPrefix;
        this.description = description;
    }

    public byte[] p2pkhPrefix() {
        return p2pkhPrefix;
    }

    public byte[] p2shPrefix() {
        return p2shPrefix;
    }

    public String description() {
        return description;
    }

    public static final Predicate<NetworkType> isMainnet = (n) -> n.equals(MAINNET);
    public static final Predicate<NetworkType> isRegtest = (n) -> n.equals(REGTEST);
    public static final Predicate<NetworkType> isTestnet = (n) -> n.equals(TESTNET3);

    public static final Function<NetworkType, byte[]> p2pkhNetworkPrefix = (n) -> {
        switch (n) {
            case MAINNET:
                return MAINNET.p2pkhPrefix;
            case REGTEST:
                return REGTEST.p2pkhPrefix;
            case TESTNET3:
                return TESTNET3.p2pkhPrefix;
            default:
                throw new IllegalStateException("Unknown NetworkType " + n);
        }
    };

    public static final Function<NetworkType, byte[]> p2shNetPrefix = (n) -> {
        switch (n) {
            case MAINNET:
                return MAINNET.p2shPrefix;
            case REGTEST:
                return REGTEST.p2shPrefix;
            case TESTNET3:
                return TESTNET3.p2shPrefix;
            default:
                throw new IllegalStateException("Unknown NetworkType " + n);
        }
    };


    @Override
    public String toString() {
        return "NetworkType{" +
                "name='" + this.name() +
                ", p2pkhPrefix=" + HEX.byteToPrefixedHex.apply(p2pkhPrefix[0]) +
                ", p2pshPrefix=" + HEX.byteToPrefixedHex.apply(p2shPrefix[0]) +
                ", description='" + description + '\'' +
                '}';
    }

}
