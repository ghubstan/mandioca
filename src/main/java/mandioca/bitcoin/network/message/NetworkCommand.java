package mandioca.bitcoin.network.message;

// See https://en.bitcoin.it/wiki/Protocol_documentation

import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.stringToPaddedBytes;
import static mandioca.bitcoin.network.NetworkConstants.COMMAND_LENGTH;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class NetworkCommand {

    public static final Function<String, NetworkCommand> wrap = (s) ->
            new NetworkCommand(stringToPaddedBytes.apply(s.toLowerCase(), COMMAND_LENGTH));

    private final byte[] raw;

    public NetworkCommand(byte[] raw) {
        this.raw = raw;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public String getAscii() {
        return new String(raw).trim();
    }

    @Override
    public String toString() {
        return "NetworkCommand{" +
                "raw=" + HEX.encode(raw) +
                '}';
    }
}
