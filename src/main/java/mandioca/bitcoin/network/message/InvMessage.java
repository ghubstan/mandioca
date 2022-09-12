package mandioca.bitcoin.network.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mandioca.bitcoin.network.message.MessageType.INV;

/**
 * Allows a node to advertise its knowledge of one or more objects. It can be received unsolicited, or in reply to getblocks.
 * <p>
 * Payload (maximum 50,000 entries, which is just over 1.8 megabytes):
 * <p>
 * Field Size 	Description 	Data type 	Comments
 * 1+ 	        count 	        var_int 	Number of inventory entries
 * 36x? 	    inventory 	    inv_vect[] 	Inventory vectors
 *
 * <pre>
 * Inventory Vectors
 *
 * Inventory vectors are used for notifying other nodes about objects they have or data which is being requested.
 *
 * Inventory vectors consist of the following data format:
 * Field Size 	Description 	Data type 	Comments
 * 4 	        type 	        uint32_t 	Identifies the object type linked to this inventory
 * 32 	        hash 	        char[32] 	Hash of the object
 *
 * The object type is currently defined as one of the following possibilities:
 * Value    Name            Description
 * 0 	    ERROR           Any data of with this number may be ignored
 * 1 	    MSG_TX          Hash is related to a transaction
 * 2 	    MSG_BLOCK       Hash is related to a data block
 * 3 	    MSG_FILTERED_BLOCK 	Hash of a block header; identical to MSG_BLOCK. Only to be used in getdata message.
 *              Indicates the reply should be a merkleblock message rather than a block message; this only works
 *              if a bloom filter has been set.
 * 4 	    MSG_CMPCT_BLOCK 	Hash of a block header; identical to MSG_BLOCK. Only to be used in getdata message.
 *              Indicates the reply should be a cmpctblock message. See BIP 152 for more info.
 *
 * Other Data Type values are considered reserved for future implementations.
 * </pre>
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#inv">https://en.bitcoin.it/wiki/Protocol_documentation#inv</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#getdata">https://en.bitcoin.it/wiki/Protocol_documentation#getdata</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors">https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors</a>
 */
public class InvMessage extends AbstractNetworkMessage implements NetworkMessage {

    private final byte[] count; // varint, number of inventory entries
    private final byte[] inventory; // variable length (36x?), inventory vectors

    public InvMessage(byte[] count, byte[] inventory) {
        this.messageType = INV;
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
            throw new RuntimeException("error serializing inv message", e);
        }
    }
}
