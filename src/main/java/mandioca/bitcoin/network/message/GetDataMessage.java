package mandioca.bitcoin.network.message;

import mandioca.bitcoin.util.Tuple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static mandioca.bitcoin.function.EndianFunctions.reverse;
import static mandioca.bitcoin.network.message.MessageType.GETDATA;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

/**
 * Used in response to inv, to retrieve the content of a specific object, and is usually sent after receiving an inv
 * packet, after filtering known elements. It can be used to retrieve transactions, but only if they are in the
 * memory pool or relay set - arbitrary access to transactions in the chain is not allowed to avoid having clients
 * start to depend on nodes having full transaction indexes (which modern nodes do not).
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#getdata">https://en.bitcoin.it/wiki/Protocol_documentation#getdata</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors">https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors</a>
 */
public class GetDataMessage extends AbstractNetworkMessage implements NetworkMessage {

    // Payload (maximum 50,000 entries, which is just over 1.8 megabytes):  1887436.8 bytes 1.8 MB is max msg size

    // class GetDataMessage:
    //        command = b'getdata'
    //        def __init__(self):
    //            self.data = []  # <1>

    private final List<Tuple<byte[], byte[]>> inventoryItems;

    private byte[] count; // varint, number of inventory entries
    /**
     * Inventory vectors are used for notifying other nodes about objects they have or data which is being requested.
     * <p>
     * Inventory vectors consist of the following data format:
     * Field Size 	Description 	Data type 	Comments
     * 4 	        type 	        uint32_t 	Identifies the object type linked to this inventory
     * 32 	        hash 	        char[32] 	Hash of the object
     */
    private byte[] inventory; // variable length (36x?), inventory vectors

    public GetDataMessage() {
        this.messageType = GETDATA;
        inventoryItems = new ArrayList<>();
    }

    public void add(InventoryObjectType type, byte[] item) {
        // def add_data(self, data_type, identifier):
        //        self.data.append((data_type, identifier))  # <2>
        String hex = HEX.encode(item);
        inventoryItems.add(new Tuple<>(type.type, item));
    }

    @Override
    public byte[] serialize() {
        //  def serialize(self):
        //        # start with the number of items as a varint
        //        result = encode_varint(len(self.data))
        //        # loop through each tuple (data_type, identifier) in self.data
        //        for data_type, identifier in self.data:
        //            # data type is 4 bytes Little-Endian
        //            result += int_to_little_endian(data_type, 4)
        //            # identifier needs to be in Little-Endian
        //            result += identifier[::-1]
        //        return result
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] sizeVarint = VARINT.encode(inventoryItems.size());
            baos.write(sizeVarint);                         // what I want: 0xfdd007
            inventoryItems.stream().forEachOrdered(item -> {
                try {
                    baos.write(item.getX()); // type is already little endian
                    baos.write(reverse.apply(item.getY())); // reverse hash
                } catch (IOException e) {
                    throw new RuntimeException("error serializing getdata item ", e);
                }
            });
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing getdata message", e);
        }
    }

}
