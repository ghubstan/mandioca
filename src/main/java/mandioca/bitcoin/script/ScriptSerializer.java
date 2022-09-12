package mandioca.bitcoin.script;

import mandioca.bitcoin.script.processing.OpCode;

import java.io.ByteArrayOutputStream;
import java.util.function.Function;
import java.util.function.Predicate;

import static mandioca.bitcoin.function.ByteArrayFunctions.concatenate;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;
import static mandioca.bitcoin.script.processing.Op.getOpCodeAsInt;
import static mandioca.bitcoin.script.processing.OpCode.OP_PUSHDATA1;
import static mandioca.bitcoin.script.processing.OpCode.OP_PUSHDATA2;
import static mandioca.bitcoin.script.processing.ScriptConstants.MAX_SCRIPT_ELEMENT_SIZE;
import static mandioca.bitcoin.util.VarintUtils.VARINT;

public class ScriptSerializer {

    //  for serializing script-sig or script-pubkey
    public static final Function<byte[], byte[]> serializeScriptField = (b) -> concatenate.apply(VARINT.encode(b.length), b);

    private final Predicate<Integer> isCommand = (l) -> l < 75;
    private final Predicate<Integer> isPushData1 = (l) -> (l > 75) && (l < 0x100);     //    256
    private final Predicate<Integer> isPushData2 = (l) -> (l >= 0x100) && (l <= 520);  // 0x0208
    private Script script;

    public ScriptSerializer() {
    }

    public ScriptSerializer init(Script script) {
        this.script = script;
        return this;
    }

    /**
     * Returns the byte serialization of the Script as variable length byte array.
     *
     * @return byte[]
     */
    public byte[] serialize() {
        byte[] result = rawSerialize(); // need to serializeInternal 1st to get the length
        byte[] fieldLength = VARINT.encode(result.length);
        return concatenate.apply(fieldLength, result);
    }

    private byte[] rawSerialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (byte[] cmd : script.getCmds()) {
                if (cmd.length == 1) {
                    baos.write(cmd[0]);  // if cmd is one byte int, it's an op code
                } else {
                    // otherwise, this is stack element
                    int length = cmd.length;
                    if (isCommand.test(length)) {
                        baos.write(toLittleEndian.apply(length, 1));
                    } else if (isPushData1.test(length)) {
                        baos.write(serializePushData(OP_PUSHDATA1, length));
                    } else if (isPushData2.test(length)) {
                        baos.write(serializePushData(OP_PUSHDATA2, length));
                    } else {
                        throw new RuntimeException("Script element > " + MAX_SCRIPT_ELEMENT_SIZE + " bytes cannot be serialized");
                    }
                    baos.write(cmd);
                }
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error serializing script commands", e);
        }
    }

    private byte[] serializePushData(OpCode opCode, int length) {
        final int arraySize;
        switch (opCode) {
            case OP_PUSHDATA1:
                arraySize = 1;
                break;
            case OP_PUSHDATA2:
                arraySize = 2;
                break;
            default:
                throw new RuntimeException("Unsupported OpCode " + opCode.name() + " (TODO)");
        }
        byte[] serializedOpCode = toLittleEndian.apply(getOpCodeAsInt(opCode), arraySize);
        return concatenate.apply(serializedOpCode, toLittleEndian.apply(length, arraySize));
    }
}
