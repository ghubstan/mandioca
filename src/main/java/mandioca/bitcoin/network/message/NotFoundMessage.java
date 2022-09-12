package mandioca.bitcoin.network.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mandioca.bitcoin.network.message.MessageType.NOTFOUND;

/**
 * A response to a getdata, sent if any requested data items could not be relayed, for example, because the
 * requested transaction was not in the memory pool or relay set.
 * <p>
 * Field Size 	Description 	Data type 	Comments
 * 1+ 	        count 	        var_int 	Number of inventory entries
 * 36x? 	    inventory 	    inv_vect[] 	Inventory vectors
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#notfound">https://en.bitcoin.it/wiki/Protocol_documentation#notfound</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#getdata">https://en.bitcoin.it/wiki/Protocol_documentation#getdata</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors">https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors</a>
 */
public class NotFoundMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] count; // varint, number of inventory entries
    private final byte[] inventory; // variable length (36x?), inventory vectors

    public NotFoundMessage(byte[] count, byte[] inventory) {
        this.messageType = NOTFOUND;
        this.count = count;
        this.inventory = inventory;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(count);
            baos.write(inventory);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing getdata message", e);
        }
    }
}
