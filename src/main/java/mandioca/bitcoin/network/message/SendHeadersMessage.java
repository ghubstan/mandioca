package mandioca.bitcoin.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.function.ByteArrayFunctions.toByteArrayInputStream;
import static mandioca.bitcoin.network.message.MessageType.SENDHEADERS;

/**
 * Request for Direct headers announcement.
 * <p>
 * Upon receipt of this message, the node is be permitted, but not required, to announce new blocks by headers command (instead of inv command).
 * <p>
 * This message is supported by the protocol version >= 70012 or Bitcoin Core version >= 0.12.0.
 * <p>
 * See <a href="https://github.com/bitcoin/bips/blob/master/bip-0130.mediawiki">BIP 130</a> for more information.
 * <p>
 * No additional data is transmitted with this message.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#sendheaders">https://en.bitcoin.it/wiki/Protocol_documentation#sendheaders</a>
 */
public final class SendHeadersMessage extends AbstractNetworkMessage implements NetworkMessage {

    private static final Logger log = LoggerFactory.getLogger(SendHeadersMessage.class);

    private static final Function<byte[], ByteArrayInputStream> stream = toByteArrayInputStream;

    public SendHeadersMessage() {
        this.messageType = SENDHEADERS;
    }

    @Override
    public byte[] serialize() {
        return emptyArray.apply(0);
    }
}
