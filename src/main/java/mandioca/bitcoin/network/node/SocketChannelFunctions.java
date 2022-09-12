package mandioca.bitcoin.network.node;

import mandioca.bitcoin.function.ThrowingFunction;

import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.System.identityHashCode;
import static mandioca.bitcoin.network.NetworkProperties.LOCALHOST;

public class SocketChannelFunctions {

    public static final BiFunction<Integer, Integer, String[]> createLocalPeerList = (port, n) -> {
        // TODO  replace this with config file containing local peer list in p1,p2,p3,p4 format
        List<String> pList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            pList.add(LOCALHOST + ":" + (port + i));
        }
        return pList.toArray(new String[]{});
    };

    private static final String channelInfoStringFormat = "obj-id:%-11d remote-port:%d local-port:%d";
    // use formatter sparingly, and always check log.isDebugEnabled before executing log.debug statements
    public static final ThrowingFunction<SelectableChannel, String> channelInfo = (sc) ->
            format(channelInfoStringFormat, identityHashCode(sc),
                    ((InetSocketAddress) ((SocketChannel) sc).getRemoteAddress()).getPort(),
                    ((InetSocketAddress) ((SocketChannel) sc).getLocalAddress()).getPort());

    public static final ThrowingFunction<SelectableChannel, String> channelToCacheKey = (sc) ->
            format("%s:%s",
                    ((InetSocketAddress) ((SocketChannel) sc).getRemoteAddress()).getHostString(),
                    ((InetSocketAddress) ((SocketChannel) sc).getRemoteAddress()).getPort());


    public static final Function<InetSocketAddress, String> addressInfo = (a) -> format("%s:%s", a.getHostName(), a.getPort());

    public static final Function<InetSocketAddress, String> addressToCacheKey = (a) ->
            format("%s:%s", a.getHostName(), a.getPort());

    /*
    public static final ThrowingFunction<SelectionKey, InetSocketAddress> selectionKeyLocalAddress = (selectionKey) ->
            (InetSocketAddress) ((SocketChannel) selectionKey.channel()).getLocalAddress();
     */
}
