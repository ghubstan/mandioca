package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.network.message.MessageType.FILTERADD;

/**
 * Bloom filtering of connections as defined in BIP 0037.
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#filterload.2C_filteradd.2C_filterclear.2C_merkleblock">
 * https://en.bitcoin.it/wiki/Protocol_documentation#filterload.2C_filteradd.2C_filterclear.2C_merkleblock</a>
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0037.mediawiki">BIP 0037</a>
 */
public class FilterAddMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] data; // arbitrary sized data element to add to the current filter

    public FilterAddMessage(byte[] data) {
        this.messageType = FILTERADD;
        this.data = data;
    }

    @Override
    public byte[] serialize() {
        return data;
    }

}
