package mandioca.bitcoin.ecc;

import mandioca.bitcoin.util.Tuple;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static java.math.BigInteger.*;
import static mandioca.bitcoin.function.BigIntegerFunctions.wrap;
import static org.junit.Assert.assertEquals;

public class EllipticCurvePointTest extends AbstractEllipticCurveTest {

    @Test
    public void testValidAndInvalidPointsOnCurveOverOrder103() {
        EllipticCurve ec = getCurve(0, 7, 103);
        getPoint(ec, 17, 64);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("is not on the elliptic curve");
        getPoint(ec, 18, 65);
    }

    @Test
    public void testValidAndInvalidPointsOnCurveOverOrder223() {
        EllipticCurve ec = getCurve(0, 7, 223);
        @SuppressWarnings("rawtypes")
        List<Tuple> validPoints =
                Tuple.getList(tuple(192, 105), tuple(17, 56), tuple(1, 193));
        @SuppressWarnings("rawtypes")
        List<Tuple> invalidPoints =
                Tuple.getList(tuple(200, 119), tuple(42, 99));

        final int[] numValidPoints = new int[]{0};
        validPoints.forEach(p -> {
            ec.getPoint((BigInteger) p.getX(), (BigInteger) p.getY());
            numValidPoints[0] += 1;
        });
        assertEquals(3, numValidPoints[0]);

        invalidPoints.forEach(p -> {
            exception.expect(IllegalArgumentException.class);
            exception.expectMessage("is not on the elliptic curve");
            ec.getPoint((BigInteger) p.getX(), (BigInteger) p.getY());
        });
    }

    @Test
    public void testAddOverOrder223() {
        EllipticCurve ec = getCurve(0, 7, 223);
        Point p1 = getPoint(ec, 192, 105);
        Point p2 = getPoint(ec, 17, 56);
        Point result = p1.add(p2);
        Point expected = getPoint(ec, 170, 142);
        assertEquals(expected, result);

        p1 = getPoint(ec, 170, 142);
        p2 = getPoint(ec, 60, 139);
        result = p1.add(p2);
        expected = getPoint(ec, 220, 181);
        assertEquals(expected, result);

        p1 = getPoint(ec, 47, 71);
        p2 = getPoint(ec, 17, 56);
        result = p1.add(p2);
        expected = getPoint(ec, 215, 68);
        assertEquals(expected, result);

        p1 = getPoint(ec, 143, 98);
        p2 = getPoint(ec, 76, 66);
        result = p1.add(p2);
        expected = getPoint(ec, 47, 71);
        assertEquals(expected, result);

        p1 = getPoint(ec, 192, 105);
        result = p1.add(p1);
        expected = getPoint(ec, 49, 71);
        assertEquals(expected, result);

        p1 = getPoint(ec, 143, 98);
        result = p1.add(p1);
        expected = getPoint(ec, 64, 168);
        assertEquals(expected, result);

        p1 = getPoint(ec, 47, 71);
        result = p1.add(p1);
        expected = getPoint(ec, 36, 111);
        assertEquals(expected, result);

        p1 = getPoint(ec, 47, 71);
        result = p1.add(p1).add(p1); // 3 * p
        expected = getPoint(ec, 15, 137);
        assertEquals(expected, result);

        p1 = getPoint(ec, 47, 71);
        result = p1.add(p1).add(p1).add(p1).add(p1).add(p1).add(p1).add(p1);  // 8 * p
        expected = getPoint(ec, 116, 55);
        assertEquals(expected, result);

        p1 = getPoint(ec, 47, 71);
        result = p1.add(p1).add(p1).add(p1).add(p1).add(p1).add(p1)
                .add(p1).add(p1).add(p1).add(p1).add(p1).add(p1).add(p1)
                .add(p1).add(p1).add(p1).add(p1).add(p1).add(p1);  // 20 * p
        expected = getPoint(ec, 47, 152);
        assertEquals(expected, result);

        p1 = getPoint(ec, 47, 71);
        result = p1.add(p1).add(p1).add(p1).add(p1).add(p1).add(p1)
                .add(p1).add(p1).add(p1).add(p1).add(p1).add(p1).add(p1)
                .add(p1).add(p1).add(p1).add(p1).add(p1).add(p1).add(p1);  // 21 * p full cycle
        expected = ec.getIdentity();
        assertEquals(expected, result);
    }

    @Test
    public void testScalarMultiplyOverOrder23() {
        EllipticCurve ec = getCurve(9, 17, 23);
        Point identity = ec.getIdentity();
        Point basePoint = getPoint(ec, 16, 5);

        Point result = basePoint.scalarMultiply(ZERO);
        assertEquals(identity, result);

        result = basePoint.scalarMultiply(ONE);
        assertEquals(basePoint, result);

        result = basePoint.scalarMultiply(TWO);
        assertEquals(getPoint(ec, 20, 20), result);

        result = basePoint.scalarMultiply(wrap.apply(3));
        assertEquals(getPoint(ec, 14, 14), result);

        result = basePoint.scalarMultiply(wrap.apply(4));
        assertEquals(getPoint(ec, 19, 20), result);

        result = basePoint.scalarMultiply(wrap.apply(5));
        assertEquals(getPoint(ec, 13, 10), result);

        result = basePoint.scalarMultiply(wrap.apply(6));
        assertEquals(getPoint(ec, 7, 3), result);
        // Example taken from http://www.brainkart.com/article/Elliptic-Curve-Cryptography_844    has typo?
        //   assertEquals(getPoint(ec, 17, 32), result);

        result = basePoint.scalarMultiply(wrap.apply(7));
        assertEquals(getPoint(ec, 8, 7), result);
        // Example taken from http://www.brainkart.com/article/Elliptic-Curve-Cryptography_8443   has typo?
        //   assertEquals(getPoint(ec, 18, 72), result);

        result = basePoint.scalarMultiply(wrap.apply(8));
        assertEquals(getPoint(ec, 12, 17), result);

        result = basePoint.scalarMultiply(wrap.apply(9));
        assertEquals(getPoint(ec, 4, 5), result);
    }


    @Test
    public void testScalarMultiplyWithOrder97() {
        EllipticCurve ec = getCurve(2, 3, 97);
        Point identity = ec.getIdentity();
        Point basePoint = getPoint(ec, 3, 6);

        Point result = basePoint.scalarMultiply(ZERO);
        assertEquals(identity, result);

        result = basePoint.scalarMultiply(ONE);
        assertEquals(basePoint, result);

        result = basePoint.scalarMultiply(TWO);
        assertEquals(getPoint(ec, 80, 10), result);

        result = basePoint.scalarMultiply(wrap.apply(3));
        assertEquals(getPoint(ec, 80, 87), result);

        result = basePoint.scalarMultiply(wrap.apply(4));
        assertEquals(getPoint(ec, 3, 91), result);

        result = basePoint.scalarMultiply(wrap.apply(5));
        assertEquals(identity, result); // cycles back to identity

        result = basePoint.scalarMultiply(wrap.apply(6));
        assertEquals(getPoint(ec, 3, 6), result);

        result = basePoint.scalarMultiply(wrap.apply(7));
        assertEquals(getPoint(ec, 80, 10), result);
        // ...
        // ...
        result = basePoint.scalarMultiply(TEN);
        assertEquals(identity, result); // cycles back to identity again
    }


    @Test
    public void testScalarMultiplyWithOrder223() {
        EllipticCurve ec = getCurve(0, 7, 223);
        Point identity = ec.getIdentity();
        Point p = getPoint(ec, 47, 71);
        Point result = p.scalarMultiply(TWO);   // 2 * p
        assertEquals(getPoint(ec, 36, 111), result);

        result = p.scalarMultiply(wrap.apply(9));   // 9 * p
        assertEquals(getPoint(ec, 69, 86), result);

        result = p.scalarMultiply(wrap.apply(13));   // 13 * p
        assertEquals(getPoint(ec, 116, 168), result);

        result = p.scalarMultiply(wrap.apply(19));   // 19 * p
        assertEquals(getPoint(ec, 36, 112), result);

        result = p.scalarMultiply(wrap.apply(21));   // 21 * p
        assertEquals(identity, result);  // cycle


        p = getPoint(ec, 15, 86);  // another pt on same curve
        result = p.scalarMultiply(wrap.apply(7));
        assertEquals(identity, result);
    }

    // shorthand for creating BigIntegerTuple objects from a pair of longs.
    private Tuple<BigInteger, BigInteger> tuple(long x, long y) {
        return Tuple.valueOf(x, y);
    }
}
