package mandioca.bitcoin.ecc;

import mandioca.bitcoin.parser.ByteBufferParser;
import mandioca.bitcoin.parser.Parser;

import java.math.BigInteger;
import java.util.function.Predicate;

import static java.lang.Byte.toUnsignedInt;

class SignatureDerParser {

    static final byte[] DER_MARKER = new byte[]{(byte) 0x2};
    static final byte[] DER_PREFIX = new byte[]{(byte) 0x30};
    private static final Predicate<Byte> isNotDerPrefixByte = (b) -> toUnsignedInt(b) != toUnsignedInt(DER_PREFIX[0]);
    private static final Predicate<Byte> isNotDerMarkerByte = (b) -> toUnsignedInt(b) != toUnsignedInt(DER_MARKER[0]);
    private static final Predicate<Byte> isNotZeroByte = (b) -> toUnsignedInt(b) != 0;
    private static final Predicate<Parser> isSignatureTooLong = (p) -> p.position() < p.limit();

    private byte[] der;
    private Parser parser;

    public SignatureDerParser init(byte[] der) {
        this.der = der;
        this.parser = new ByteBufferParser(der);
        return this;
    }

    public Signature parse() {
        parseDerPrefix();
        parseSignatureLength();
        parseDerMarker();
        byte[] r = parseR();
        parseDerMarker();
        byte[] s = parseS();
        if (isSignatureTooLong.test(parser)) {
            throw new IllegalStateException("Signature too long");
        }
        return new Signature(new BigInteger(1, r), new BigInteger(1, s));
    }

    private void parseDerPrefix() {
        if (isNotDerPrefixByte.test(parser.read())) {
            throw new IllegalStateException("Bad signature");
        }
    }

    private void parseSignatureLength() {
        int sigLength = toUnsignedInt(parser.read());
        if (sigLength + 2 != der.length) {
            throw new IllegalStateException("Bad signature length");
        }
    }

    private void parseDerMarker() {
        if (isNotDerMarkerByte.test(parser.read())) {
            throw new IllegalStateException("Bad signature");
        }
    }

    private byte[] parseR() {
        int rLength = toUnsignedInt(parser.read());
        if (rLength == 33) {  // byte[] r should have been padded in der encoding because s[0] > DER_HIGH_BIT
            if (isNotZeroByte.test(parser.read())) {
                throw new IllegalStateException("Bad signature");
            } else {
                rLength = 32;
            }
        }
        return parser.readBytes(rLength);
    }

    private byte[] parseS() {
        int sLength = toUnsignedInt(parser.read());
        if (sLength == 33) {  // byte[] s should have been padded in der encoding because s[0] > DER_HIGH_BIT
            if (isNotZeroByte.test(parser.read())) {
                throw new IllegalStateException("Bad signature");
            } else {
                sLength = 32;
            }
        }
        return parser.readBytes(sLength);
    }
}
