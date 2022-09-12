package mandioca.bitcoin.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static mandioca.bitcoin.network.message.MessageType.TX;

/**
 * Describes a bitcoin transaction, in reply to getdata. When a bloom filter is applied tx objects are sent
 * automatically for matching transactions following the 'merkleblock'.
 *
 * <pre>
 * Field Size 	Description 	Data type 	Comments
 * 4 	        version 	    int32_t 	Transaction data format version (note, this is signed)
 * 0 or 2 	    flag 	        optional uint8_t[2] 	If present, always 0001, and indicates the presence of witness data
 * 1+ 	        tx_in count 	var_int 	Number of Transaction inputs (never zero)
 * 41+ 	        tx_in 	        tx_in[] 	A list of 1 or more transaction inputs or sources for coins
 * 1+ 	        tx_out count 	var_int 	Number of Transaction outputs
 * 9+ 	        tx_out 	        tx_out[] 	A list of 1 or more transaction outputs or destinations for coins
 * 0+ 	        tx_witnesses 	tx_witness[] 	A list of witnesses, one for each input; omitted if flag is omitted above
 * 4 	        lock_time 	    uint32_t 	The block number or timestamp at which this transaction is unlocked:
 *
 * Lock Time    Value 	        Description
 *              0               Not locked
 *              < 500000000 	Block number at which this transaction is unlocked
 *              >= 500000000 	UNIX timestamp at which this transaction is unlocked
 *
 * If all TxIn inputs have final (0xffffffff) sequence numbers then lock_time is irrelevant.
 * Otherwise, the transaction may not be added to a block until after lock_time (see NLockTime).
 *
 * TxIn consists of the following fields:
 * Field Size       Description 	    Data type 	Comments
 * 36 	            previous_output 	outpoint 	The previous output transaction reference, as an OutPoint structure
 * 1+ 	            script length 	    var_int 	The length of the signature script
 * ?? 	            signature script 	uchar[] 	Computational Script for confirming transaction authorization
 * 4 	            sequence 	        uint32_t 	Transaction version as defined by the sender. Intended for
 *                      "replacement" of transactions when information is updated before inclusion into a block.
 * The OutPoint structure consists of the following fields:
 * Field Size 	Description 	Data type 	Comments
 * 32 	        hash 	        char[32] 	The hash of the referenced transaction.
 * 4 	        index 	        uint32_t 	The index of the specific output in the transaction. 1st output is 0, etc.
 *
 * The Script structure consists of a series of pieces of information and operations related to the value of the transaction.
 * (Structure to be expanded in the future... see script.h and script.cpp and Script for more information)
 *
 * The TxOut structure consists of the following fields:
 * Field Size 	    Description 	    Data type 	Comments
 * 8 	            value 	            int64_t 	Transaction Value
 * 1+ 	            pk_script length    var_int 	Length of the pk_script
 * ?? 	            pk_script 	        uchar[] 	Usually contains the public key as a Bitcoin script setting up
 *                      conditions to claim this output.
 * The TxWitness structure consists of a var_int count of witness data components, followed by
 * (for each witness data component) a var_int length of the component and the raw component data itself.
 *
 * </pre>
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#tx">https://en.bitcoin.it/wiki/Protocol_documentation#tx</a>
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors">https://en.bitcoin.it/wiki/Protocol_documentation#Inventory_Vectors</a>
 */
public class TxMessage extends AbstractNetworkMessage implements NetworkMessage {

    // TODO find out if this should just be a wrapper around Tx.java

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TxMessage.class);

    private final byte[] version;       // 4 bytes, little endian identifier of protocol version being used by the node
    private final byte[] flag;          // 2 bytes (big endian?), if present, always 0001, and indicates presence of witness data
    private final byte[] txInCount;     // varint, # of transaction inputs (never zero)
    private final byte[] txIns;         // 41+ bytes, a list of 1 or more transaction inputs or sources for coins
    private final byte[] txOutCount;    // varint, # of transaction outputs
    private final byte[] txOuts;        // 9+ bytes, a list of 1 or more transaction outputs or destinations for coins
    private final byte[] txWitnesses;   // a list of witnesses, one for each input; omitted if flag is omitted above
    private final byte[] locktime;      // 4 bytes, If all TxIn inputs have final (0xffffffff) sequence numbers then lock_time
    //      is irrelevant. Otherwise, the transaction may not be added to a block until after lock_time (see NLockTime).

    public TxMessage(
            byte[] version,
            byte[] flag,
            byte[] txInCount,
            byte[] txIns,
            byte[] txOutCount,
            byte[] txOuts,
            byte[] txWitnesses,
            byte[] locktime) {
        this.messageType = TX;
        this.version = version;
        this.flag = flag;
        this.txInCount = txInCount;
        this.txIns = txIns;
        this.txOutCount = txOutCount;
        this.txOuts = txOuts;
        this.txWitnesses = txWitnesses;
        this.locktime = locktime;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // TODO when needed (using ByteBuffer)
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing tx message", e);
        }
    }

}
