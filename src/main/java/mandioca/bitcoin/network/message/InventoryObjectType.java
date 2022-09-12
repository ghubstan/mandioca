package mandioca.bitcoin.network.message;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Inventory vectors are used for notifying other nodes about objects they have or data which is being requested.
 * <p>
 * Inventory vectors consist of the following data format:
 * Field Size 	Description 	Data type 	Comments
 * 4 	        type 	        uint32_t 	Identifies the object type linked to this inventory
 * 32 	        hash 	        char[32] 	Hash of the object
 * <p>
 * This enum defines the 'object type':
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
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors">https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors</a>
 */
public enum InventoryObjectType {

    // TODO What about MSG_WITNESS_BLOCK ?

    ERROR(new byte[]{0x00, 0x00, 0x00, 0x00},
            "Any data of with this number may be ignored."),

    MSG_TX(new byte[]{0x01, 0x00, 0x00, 0x00}, "Hash is related to a transaction."),

    MSG_BLOCK(new byte[]{0x02, 0x00, 0x00, 0x00},
            "Hash is related to a data block (a normal block)."),

    MSG_FILTERED_BLOCK(new byte[]{0x03, 0x00, 0x00, 0x00},
            "Hash of a block header (a merkle block); identical to MSG_BLOCK. Only to be used in a "
                    + "getdata message.  Indicates the reply should be a merkleblock message rather than a block message;  "
                    + "this only works if a bloom filter has been set"),

    MSG_CMPCT_BLOCK(new byte[]{0x04, 0x00, 0x00, 0x00},
            "Hash of a block header (a compact block); identical to MSG_BLOCK. Only to be used in getdata message.  "
                    + "Indicates the reply should be a cmpctblock message. See BIP 152 for more info.");

    public static final Function<byte[], InventoryObjectType> inventoryObjectType = (b) -> {
        if (Arrays.equals(b, ERROR.type)) {
            return ERROR;
        } else if (Arrays.equals(b, MSG_TX.type)) {
            return MSG_TX;
        } else if (Arrays.equals(b, MSG_BLOCK.type)) {
            return MSG_BLOCK;
        } else if (Arrays.equals(b, MSG_FILTERED_BLOCK.type)) {
            return MSG_FILTERED_BLOCK;
        } else if (Arrays.equals(b, MSG_CMPCT_BLOCK.type)) {
            return MSG_CMPCT_BLOCK;
        } else {
            throw new IllegalArgumentException("unsupported inventory object type " + Arrays.toString(b));
        }
    };

    final byte[] type;
    final String description;

    InventoryObjectType(byte[] type, String description) {
        this.type = type;
        this.description = description;
    }

    public byte[] type() {
        return type;
    }

    public String description() {
        return description;
    }
}
