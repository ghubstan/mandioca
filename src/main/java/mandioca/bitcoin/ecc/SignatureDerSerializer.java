package mandioca.bitcoin.ecc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.function.Predicate;

import static java.lang.Byte.toUnsignedInt;
import static mandioca.bitcoin.ecc.SignatureDerParser.DER_MARKER;
import static mandioca.bitcoin.ecc.SignatureDerParser.DER_PREFIX;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;

class SignatureDerSerializer {

    private static final byte DER_HIGH_BYTE = (byte) 0x80; // not a byte[], not concatenated during encoding
    private static final Predicate<Byte> isDerHighByte = (b) -> toUnsignedInt(b) >= toUnsignedInt(DER_HIGH_BYTE);

    private Signature signature;

    public SignatureDerSerializer() {
    }

    public SignatureDerSerializer init(Signature signature) {
        this.signature = signature;
        return this;
    }

    public byte[] getDer() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writeCoordinateToDerStream(signature.getR(), baos);
            writeCoordinateToDerStream(signature.getS(), baos);
            byte[] result = baos.toByteArray(); // cache un-prefixed result
            baos.reset();                       // re-use byte output stream after caching un-prefixed result
            baos.write(DER_PREFIX);
            baos.write(result.length);          // encode entire sig's length, 0x44 (68 base10), or 0x45 (69 base10)
            baos.write(result);
            return baos.toByteArray();          // returning bytes([0x30, len(result)]) + result
        } catch (IOException e) {
            throw new RuntimeException("Error parsing address from public key", e);
        }
    }

    private void writeCoordinateToDerStream(BigInteger coordinate, final ByteArrayOutputStream baos) throws IOException {
        byte[] bytes = bigIntToUnsignedByteArray.apply(coordinate);
        if (isDerHighByte.test(bytes[0])) {
            bytes = concatenate.apply(ZERO_BYTE, bytes);  // if s[0] is high bit, add 0x0
        }
        baos.write(concatenate.apply(DER_MARKER, new byte[]{(byte) bytes.length}));  // encode s length
        baos.write(bytes);
    }
}
