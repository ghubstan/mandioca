package mandioca.bitcoin.network.message;

import static mandioca.bitcoin.function.ByteArrayFunctions.emptyArray;
import static mandioca.bitcoin.network.message.MessageType.REJECT;

/**
 * The reject message is sent when messages are rejected
 *
 * @see <a href="https://en.bitcoin.it/wiki/Protocol_documentation#reject">https://en.bitcoin.it/wiki/Protocol_documentation#reject</a>
 */
public final class RejectMessage extends AbstractNetworkMessage implements NetworkMessage {

    // TODO impl

    private final String message;       // type of message rejected
    private final char ccode;           // code relating to rejected message
    private final String reason;        // text version of reason for rejection
    private final byte[] data;          // optional extra data provided by some errors. Currently, all errors which provide this field fill it with the TXID or block header hash of the object being rejected, so the field is 32 bytes.


    public RejectMessage(String message, char ccode, String reason, byte[] data) {
        this.messageType = REJECT;
        this.message = message;
        this.ccode = ccode;
        this.reason = reason;
        this.data = data;
    }

    @Override
    public byte[] serialize() {
        return emptyArray.apply(0);
    }

    // CCodes
    //Value 	Name 	Description
    //0x01 	    REJECT_MALFORMED
    //0x10 	    REJECT_INVALID
    //0x11 	    REJECT_OBSOLETE
    //0x12 	    REJECT_DUPLICATE
    //0x40 	    REJECT_NONSTANDARD
    //0x41 	    REJECT_DUST
    //0x42 	    REJECT_INSUFFICIENTFEE
    //0x43 	    REJECT_CHECKPOINT

    public enum CCodes {
        REJECT_MALFORMED,
        REJECT_INVALID,
        REJECT_OBSOLETE,
        REJECT_DUPLICATE,
        REJECT_NONSTANDARD,
        REJECT_DUST,
        REJECT_INSUFFICIENTFEE,
        REJECT_CHECKPOINT,
    }
}


