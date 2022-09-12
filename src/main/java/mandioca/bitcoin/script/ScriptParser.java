package mandioca.bitcoin.script;

import mandioca.bitcoin.parser.ByteBufferParser;
import mandioca.bitcoin.parser.DataInputStreamParser;
import mandioca.bitcoin.parser.Parser;
import mandioca.bitcoin.script.processing.OpCode;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static mandioca.bitcoin.function.ByteArrayFunctions.bytesToInt;
import static mandioca.bitcoin.function.ByteCompareFunctions.isInRangeInclusive;
import static mandioca.bitcoin.script.processing.Op.isOpCode;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class ScriptParser {

    private final Predicate<Byte> isCommand = (b) -> isInRangeInclusive.apply(b, new byte[]{(byte) 0x01, (byte) 0x4b});  // 1 >= b <= 75

    private final Predicate<Byte> isPushData1 = (b) -> isOpCode.apply(b, OP_PUSHDATA1);
    private final Predicate<Byte> isPushData2 = (b) -> isOpCode.apply(b, OP_PUSHDATA2);
    private final Predicate<Byte> isInvalidOp = (b) -> isOpCode.apply(b, OP_INVALIDOPCODE);

    private Parser parser;

    public ScriptParser init(byte[] scriptPubKey) {
        this.parser = new ByteBufferParser(scriptPubKey);
        return this;
    }

    public ScriptParser init(DataInputStream is) {
        this.parser = new DataInputStreamParser(is);
        return this;
    }

    // TODO Is this even used anymore?  Should it parse specific script types instead of generic?
    public Script parse() {
        final List<byte[]> cmdList = parseCommands();
        return new Script(cmdList.toArray(new byte[0][]));
    }

    // Parse script sig for inputs
    // Parse script pub key for outputs
    // Both are parsed the same way
    private List<byte[]> parseCommands() {
        final List<byte[]> cmdList = new ArrayList<>(); // each cmd is an opcode to be executed or pushed onto stack
        final long scriptLength = parser.readVarint();  // script serialization always begins with length of entire script
        byte current;

        while (parser.getDecodedByteCount() < scriptLength) {

            current = parser.read(); // current is opcode or stack element
            parser.incrementDecodedByteCount(1);
            if (isInvalidOp.test(current)) {
                System.err.println("Current byte is OP_INVALID, WTF?  Do I just ignore it?  Log it?  Throw up?");
            } else {
                if (isCommand.test(current)) {          // next opcode bytes is data to be pushed onto the stack
                    cmdList.add(parseCommand(current)); // current byte is between 0x01 and 0x4b, stack element 'n'
                } else if (isPushData1.test(current)) {
                    cmdList.add(parsePushData(OP_PUSHDATA1));
                } else if (isPushData2.test(current)) {
                    cmdList.add(parsePushData(OP_PUSHDATA2));
                } else {
                    cmdList.add(new byte[]{current});   // current byte is an op code
                }
            }
        }
        if (parser.getDecodedByteCount() != scriptLength) {
            throw new RuntimeException("Error parsing script commands");
        }
        return cmdList;
    }

    private byte[] parseCommand(byte n) {
        byte[] cmd = parser.readBytes(n);                 // add the next n bytes as a cmd
        parser.incrementDecodedByteCount(n);              // increase count by current byte
        // DEBUG
        String cmdHex = HEX.encode(cmd);
        // END DEBUG
        return cmd;
    }

    private byte[] parsePushData(OpCode opCode) {
        final int bufSize, decodeDataLength;
        switch (opCode) {
            case OP_PUSHDATA1:
                bufSize = 1;    // next byte determines # bytes to read
                break;
            case OP_PUSHDATA2:
                bufSize = 2;    // next 2 bytes determines # bytes to read
                break;
            default:
                throw new RuntimeException("Unsupported OpCode " + opCode.name());
        }
        decodeDataLength = bytesToInt.apply(parser.readBytes(bufSize)); // find out how many bytes to read
        byte[] bytes = parser.readBytes(decodeDataLength);
        parser.incrementDecodedByteCount(decodeDataLength + bufSize);
        return bytes;
    }
}
