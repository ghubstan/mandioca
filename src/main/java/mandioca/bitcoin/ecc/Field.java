package mandioca.bitcoin.ecc;

import java.math.BigInteger;

// Java Example, using BigInteger:
// https://github.com/trident2710/Crypto/tree/master/src/main/java/com/trident/crypto/field
// Talk & White Papers
// https://sites.math.washington.edu/~morrow/336_12/papers/juan.pdf
// http://cdn.intechopen.com/pdfs/29704/InTech-Division_and_inversion_over_finite_fields.pdf
// https://www.ijert.org/implementation-of-finite-field-arithmetic-operations-for-large-prime-and-binary-fields-using-java-biginteger-class
// https://jeremykun.com/2014/03/13/programming-with-finite-fields
// http://ringsalgebra.io
// https://math.stackexchange.com/questions/1000197/java-efficient-implementation-of-a-finite-field-for-cryptography-use
//
// TODO Credit https://github.com/trident2710/Crypto where applicable
public interface Field extends Cloneable {

    BigInteger getNumber();

    BigInteger getPrime();

    Field add(Field other);

    Field subtract(Field other);

    Field multiply(Field other);

    Field divide(Field other);

    Field power(BigInteger exponent);

    Field sqrt();

    boolean notEquals(Field o);
}
