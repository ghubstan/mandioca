package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.function.ThrowingFunction;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mandioca.bitcoin.function.ByteCompareFunctions.isEqual;
import static mandioca.bitcoin.function.EndianFunctions.toBigEndian;
import static mandioca.bitcoin.function.EndianFunctions.toLittleEndian;
import static mandioca.bitcoin.util.HexUtils.HEX;


// See https://raghavsood.com/blog/2018/06/10/bitcoin-signature-types-sighash
// Function comments from https://raghavsood.com/blog/2018/06/10/bitcoin-signature-types-sighash

public enum SigHashType {

    // See https://raghavsood.com/blog/2018/06/10/bitcoin-signature-types-sighash
    /**
     * The default in every consumer-facing wallet that I am (the author is)  aware of. It signs every input and output,
     * and any change to the transaction will render the signature invalid. This essentially says "I only agree to move
     * my BTC with this exact combination of inputs and outputs".
     * <p>
     * In practice almost all transactions are signed with OP_SIGHASH_ALL.
     */
    SIGHASH_ALL((byte) 0x01, "[ALL] ",
            toLittleEndian.apply(0x01, 4),
            toBigEndian.apply(0x01, 1),
            "Default 4 byte hash type signifying signature can authorize all inputs and outputs in the transaction."),
    /**
     * This type of signature signs all inputs, and exactly one corresponding output. The corresponding output is
     * the one with the same index as your signature (i.e., if your input is at vin 0, then the output you want to
     * sign must be vout 0). This essentially says "I agree to participate in this tx with all these inputs, as long
     * as this much goes to this one address".
     * <p>
     * Is insecure.  See https://github.com/bitcoin/bitcoin/pull/13360
     */
    SIGHASH_SINGLE((byte) 0x02, "[ALL]?TODO",
            toLittleEndian.apply(0x02, 4),
            toBigEndian.apply(0x02, 1),
            "4 byte hash type signifying signature can authorize a specific output.  "
                    + "Is insecure.  See https://github.com/bitcoin/bitcoin/pull/13360"),
    /**
     * This signs all the inputs to the transaction, but none of the outputs. Effectively, it creates an authorizing
     * saying "Hey, I’m okay with participating in this transaction, but I don’t particularly care where it goes".
     * This might seem insecure, and should never be used in single-input transactions. We’ll cover why it exists soon,
     * though.
     */
    SIGHASH_NONE((byte) 0x03, "[ALL]?TODO",
            toLittleEndian.apply(0x03, 4),
            toBigEndian.apply(0x03, 1),
            "4 byte hash type signifying signature can authorize all the inputs, but none of the outputs."),

    /**
     * SIGHASH_ANYONECANPAY is combined with the next three op codes via bitwise & (effectively addition in this case).
     */
    SIGHASH_ANYONECANPAY((byte) 0x80, "[ALL]?TODO",
            toLittleEndian.apply(0x80, 4),
            toBigEndian.apply(0x80, 1),
            "TODO."),
    /**
     * This is similar to SIGHASH_ALL, and signs all outputs. However, it only signs the one input it is part of.
     * It’s essentially saying "I agree to participate in this tx, as long as the following recipients receive these
     * amounts. I don’t care about any additional inputs to this transaction."
     */
    SIGHASH_ALL_ANYONECANPAY((byte) 0x81, "[ALL]?TODO",
            toLittleEndian.apply(0x81, 4),
            toBigEndian.apply(0x81, 1),
            "TODO."),
    /**
     * This is similar to SIGHASH_NONE, but only signs the one input it is in. This is essentially like saying
     * "Hey, I’m okay with sending this BTC. In fact, I don’t even care if it is sent in this transaction.
     * Here’s a signed note saying that any transaction including this can spend this BTC".
     */
    SIGHASH_NONE_ANYONECANPAY((byte) 0x82, "[ALL]?TODO",
            toLittleEndian.apply(0x82, 4),
            toBigEndian.apply(0x82, 1),
            "TODO."),
    /**
     * This is similar to SIGHASH_SINGLE, except it only signs the input that contains it, and the corresponding output.
     * This says "I definitely want to move this much BTC to this output, but I don’t care about any other inputs and
     * outputs in this transaction".
     */
    SIGHASH_SINGLE_ANYONECANPAY((byte) 0x83, "[ALL]?TODO",
            toLittleEndian.apply(0x83, 4),
            toBigEndian.apply(0x83, 1),
            "TODO.");

    static final Predicate<Byte> isSigHashType = (b) -> {
        for (SigHashType value : SigHashType.values()) {
            if (isEqual.apply(b, value.code)) {
                return true;
            }
        }
        return false;
    };

    static final ThrowingFunction<Byte, SigHashType> getSigHashType = (b) -> {
        List<SigHashType> sigHashType = Stream.of(SigHashType.values()).filter(sht -> isEqual.apply(b, sht.code)).collect(Collectors.toList());
        if (sigHashType.size() == 0) {
            throw new RuntimeException("SigHashType " + HEX.byteToPrefixedHex.apply(b) + " is not supported");
        } else {
            return sigHashType.get(0);
        }
    };

    public static final Function<Byte, String> toSighashTypeAsmToken = (b) -> getSigHashType.apply(b).asmtoken;

    final byte code;
    final String asmtoken;
    final byte[] littleEndian;
    final byte[] bigEndian;
    final String description;

    SigHashType(byte code, String asmtoken, byte[] littleEndian, byte[] bigEndian, String description) {
        this.code = code;
        this.asmtoken = asmtoken;
        this.littleEndian = littleEndian;
        this.bigEndian = bigEndian;
        this.description = description;
    }

    public byte code() {
        return this.code;
    }

    public String asmtoken() {
        return this.asmtoken;
    }

    public byte[] littleEndian() {
        return this.littleEndian;
    }

    public byte[] bigEndian() {
        return this.bigEndian;
    }

    public String description() {
        return this.description;
    }

    @Override
    public String toString() {
        return "SigHashType{" +
                "name=" + this.name() +
                "  code=" + code +
                "  asmtoken=" + asmtoken +
                "  littleEndian=" + HEX.encode(littleEndian) +
                "  bigEndian=" + HEX.encode(bigEndian) +
                ", description='" + description + '\'' +
                '}';
    }
}

