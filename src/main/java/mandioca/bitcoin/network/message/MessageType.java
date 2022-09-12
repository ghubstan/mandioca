package mandioca.bitcoin.network.message;

import java.util.function.Function;

import static mandioca.bitcoin.network.message.NetworkCommand.wrap;

/**
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Message_types">https://en.bitcoin.it/wiki/Protocol_documentation#Message_types</a>
 */
public enum MessageType {

    ERROR(wrap.apply("error"), "diagnostic error details -- not part of bitcoin's network protocol"),
    BLOCK(wrap.apply("block"), "response to a getdata message which requests transaction information from a block hash"),
    FEEFILTER(wrap.apply("feefilter"), "minimal feerate in satoshis per 1000 bytes"),
    FILTERADD(wrap.apply("filteradd"), "data element to add to the current filter"),
    FILTERCLEAR(wrap.apply("filterclear"), "clear bloom filter"),
    FILTERLOAD(wrap.apply("filterload"), "details needed for loading bloom filter"),
    GENERIC(wrap.apply("generic"), "multipurpose, so far, just BloomFilter.filterLoad()"), // TODO
    GETBLOCKS(wrap.apply("getblocks"), "requests inv packet containing list of blocks starting right after the last known hash in the block locator object, up to hash_stop or 500 blocks, whichever comes first"),
    GETDATA(wrap.apply("getdata"), "getdata is used in response to inv, to retrieve the content of a specific object, and is usually sent after receiving an inv packet"),
    GETHEADERS(wrap.apply("getheaders"), "headers pkt of the headers of blocks starting right after the last known hash in the blockLocator object, up to hashStop or 2k blocks"),
    HEADERS(wrap.apply("headers"), "block headers in response to a getheaders packet"),
    INV(wrap.apply("inv"), "advertise knowledge of one or more objects"),
    MERKLEBLOCK(wrap.apply("merkleblock"), "information needed to verify a transaction is in the merkle tree"),
    NOTFOUND(wrap.apply("notfound"), "response to a getdata if any requested data items could not be relayed"),
    PING(wrap.apply("ping"), "confirm that the TCP/IP connection is still valid"),
    PONG(wrap.apply("pong"), "sent in response to a ping message, using a nonce included in the ping"),
    REJECT(wrap.apply("reject"), "sent when messages are rejected"),
    SENDCMPCT(wrap.apply("sendcmpct"), "determines if node is 'high-bandwidth' enabled or not"),
    SENDHEADERS(wrap.apply("sendheaders"), "request for direct headers announcement"),
    TX(wrap.apply("tx"), "describes a bitcoin transaction, in reply to getdata"),
    VERSION(wrap.apply("version"),
            "on connect, version and verack messages are exchanged, in order to ensure compatibility between peers"),
    VERACK(wrap.apply("verack"),
            "sent in reply to version"),
    UNKNOWN(wrap.apply("unknown"),
            "unknown message");;

    public static final Function<NetworkCommand, MessageType> getMessageType = (c) -> {
        String cmdName = c.getAscii();

        /*
        // TODO ?
        switch (cmdName) {
            default:
                break;
        }
        */

        if (cmdName.equalsIgnoreCase(ERROR.name())) {
            return ERROR;
        } else if (cmdName.equalsIgnoreCase(BLOCK.name())) {
            return BLOCK;
        } else if (cmdName.equalsIgnoreCase(FEEFILTER.name())) {
            return FEEFILTER;
        } else if (cmdName.equalsIgnoreCase(FILTERADD.name())) {
            return FILTERADD;
        } else if (cmdName.equalsIgnoreCase(FILTERCLEAR.name())) {
            return FILTERCLEAR;
        } else if (cmdName.equalsIgnoreCase(FILTERLOAD.name())) {
            return FILTERLOAD;
        } else if (cmdName.equalsIgnoreCase(GENERIC.name())) {
            return GENERIC;
        } else if (cmdName.equalsIgnoreCase(GETBLOCKS.name())) {
            return GETBLOCKS;
        } else if (cmdName.equalsIgnoreCase(GETHEADERS.name())) {
            return GETDATA;
        } else if (cmdName.equalsIgnoreCase(GETDATA.name())) {
            return GETHEADERS;
        } else if (cmdName.equalsIgnoreCase(HEADERS.name())) {
            return HEADERS;
        } else if (cmdName.equalsIgnoreCase(INV.name())) {
            return INV;
        } else if (cmdName.equalsIgnoreCase(MERKLEBLOCK.name())) {
            return MERKLEBLOCK;
        } else if (cmdName.equalsIgnoreCase(NOTFOUND.name())) {
            return NOTFOUND;
        } else if (cmdName.equalsIgnoreCase(PING.name())) {
            return PING;
        } else if (cmdName.equalsIgnoreCase(PONG.name())) {
            return PONG;
        } else if (cmdName.equalsIgnoreCase(REJECT.name())) {
            return REJECT;
        } else if (cmdName.equalsIgnoreCase(SENDCMPCT.name())) {
            return SENDCMPCT;
        } else if (cmdName.equalsIgnoreCase(SENDHEADERS.name())) {
            return SENDHEADERS;
        } else if (cmdName.equalsIgnoreCase(TX.name())) {
            return TX;
        } else if (cmdName.equalsIgnoreCase(VERSION.name())) {
            return VERSION;
        } else if (cmdName.equalsIgnoreCase(VERACK.name())) {
            return VERACK;
        } else if (cmdName.trim().equalsIgnoreCase(UNKNOWN.name())) {
            return UNKNOWN;
        } else {
            throw new IllegalArgumentException("unsupported message type " + cmdName);
        }
    };

    final NetworkCommand command;
    final String description;

    MessageType(NetworkCommand command, String description) {
        this.command = command;
        this.description = description;
    }

    public NetworkCommand command() {
        return this.command;
    }

    public String description() {
        return this.description;
    }

}




