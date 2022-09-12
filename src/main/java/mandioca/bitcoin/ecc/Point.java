package mandioca.bitcoin.ecc;

import java.math.BigInteger;

// TODO Credit https://github.com/trident2710/Crypto where applicable
public interface Point extends Cloneable {

    Point add(Point other);

    Point scalarMultiply(BigInteger coefficient);

    Field getX();

    Field getY();

    Field getA();

    Field getB();

    boolean notEquals(Object o);
}
