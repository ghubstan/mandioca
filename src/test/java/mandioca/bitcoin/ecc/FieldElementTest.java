package mandioca.bitcoin.ecc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static java.math.BigInteger.TWO;
import static mandioca.bitcoin.ecc.FieldElement.valueOf;
import static org.junit.Assert.*;

public class FieldElementTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testConstructorNegativeNumberArgException() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Number -1 not in field range 0 to 22");
        valueOf(-1, 23);
    }

    @Test
    public void testConstructorNumberGreaterThanOrderException() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Number 24 not in field range 0 to 22");
        valueOf(24, 23);
    }

    @Test
    public void testEquals() {
        Field a = valueOf(11, 43);
        Field b = valueOf(11, 43);
        assertEquals(a, b);
    }

    @Test
    public void testNotEquals() {
        Field a = valueOf(2, 31);
        Field b = valueOf(2, 31);
        Field c = valueOf(15, 31);
        assertTrue(a.notEquals(c));
        //noinspection SimplifiableJUnitAssertion
        assertFalse(!a.notEquals(c));
        assertFalse(a.notEquals(b));
        //noinspection SimplifiableJUnitAssertion
        assertTrue(!a.notEquals(b));
    }

    @Test
    public void testAddNullArgumentException() {
        Field a = valueOf(5, 23);
        exception.expect(NullPointerException.class);
        exception.expectMessage("Cannot operate on null Field");
        a.add(null);
    }

    @Test
    public void testAddArgumentException() {
        Field a = valueOf(5, 23);
        Field b = valueOf(17, 19);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot add two numbers in different finite fields");
        a.add(b);
    }

    @Test
    public void testAdd() {
        FieldElement a = valueOf(5, 23);
        Field b = valueOf(17, 23);
        Field c = a.add(b);
        assertEquals(valueOf(22, 23), c);
        Field d = valueOf(22, 23);
        //noinspection SimplifiableJUnitAssertion
        assertTrue(c.equals(d));

        a = valueOf(18, 23);
        b = valueOf(9, 23);
        c = a.add(b);
        assertEquals(valueOf(4, 23), c);

        a = valueOf(7, 19);
        b = valueOf(8, 19);
        c = a.add(b);
        assertEquals(valueOf(15, 19), c);

        a = valueOf(11, 19);
        b = valueOf(17, 19);
        c = a.add(b);
        assertEquals(valueOf(9, 19), c);

        a = valueOf(44, 57);
        b = valueOf(33, 57);
        c = a.add(b);
        assertEquals(valueOf(20, 57), c);

        a = valueOf(17, 57);
        b = valueOf(42, 57);
        c = valueOf(49, 57);
        d = a.add(b, c);
        assertEquals(valueOf(51, 57), d);
    }

    @Test
    public void testSubtract() {
        FieldElement a = valueOf(9, 57);
        Field b = valueOf(29, 57);
        Field c = a.subtract(b);
        assertEquals(valueOf(37, 57), c);

        a = valueOf(7, 23);
        b = valueOf(14, 23);
        c = a.subtract(b);
        assertEquals(valueOf(16, 23), c);

        a = valueOf(29, 31);
        b = valueOf(4, 31);
        c = a.subtract(b);
        assertEquals(valueOf(25, 31), c);

        a = valueOf(15, 31);
        b = valueOf(30, 31);
        c = a.subtract(b);
        assertEquals(valueOf(16, 31), c);

        a = valueOf(52, 57);
        b = valueOf(30, 57);
        c = valueOf(38, 57);
        Field d = a.subtract(b, c);
        assertEquals(valueOf(41, 57), d);
    }


    @Test
    public void testMultiply() {
        FieldElement a = valueOf(24, 31);
        Field b = valueOf(19, 31);
        Field c = a.multiply(b);
        assertEquals(valueOf(22, 31), c);

        a = valueOf(5, 19);
        b = valueOf(3, 19);
        c = a.multiply(b);
        assertEquals(valueOf(15, 19), c);

        a = valueOf(8, 19);
        b = valueOf(17, 19);
        c = a.multiply(b);
        assertEquals(valueOf(3, 19), c);

        a = valueOf(4, 23);
        b = valueOf(7, 23);
        c = a.multiply(b);
        assertEquals(valueOf(5, 23), c);

        a = valueOf(3, 13);
        b = valueOf(12, 13);
        c = a.multiply(b);
        assertEquals(valueOf(10, 13), c);

        a = valueOf(95, 97);
        b = valueOf(45, 97);
        c = valueOf(31, 97);
        Field d = a.multiply(b, c);
        assertEquals(valueOf(23, 97), d);

        a = valueOf(17, 97);
        b = valueOf(13, 97);
        c = valueOf(19, 97);
        d = valueOf(44, 97);
        Field e = a.multiply(b, c, d);
        assertEquals(valueOf(68, 97), e);
    }

    @Test
    public void testExponentiate() {
        Field a = valueOf(17, 31);
        Field b = a.power(BigInteger.valueOf(3));
        assertEquals(valueOf(15, 31), b);

        a = valueOf(5, 31);
        b = valueOf(18, 31);
        Field c = a.power(BigInteger.valueOf(5)).multiply(b);
        assertEquals(valueOf(16, 31), c);

        a = valueOf(7, 19);
        b = a.power(BigInteger.valueOf(3));
        assertEquals(valueOf(1, 19), b);

        a = valueOf(9, 19);
        b = a.power(BigInteger.valueOf(12));
        assertEquals(valueOf(7, 19), b);

        a = valueOf(3, 13);
        b = a.power(BigInteger.valueOf(3));
        assertEquals(valueOf(1, 13), b);

        a = valueOf(64, 103);
        b = a.power(TWO);
        assertEquals(valueOf(79, 103), b);

        a = valueOf(17, 103);
        b = valueOf(7, 103);
        c = a.power(BigInteger.valueOf(3)).add(b);
        assertEquals(valueOf(79, 103), c);

        a = valueOf(17, 31);
        b = a.power(BigInteger.valueOf(-3)); // negative exponent
        assertEquals(valueOf(29, 31), b);

        a = valueOf(4, 31).power(-4); // negative exponent;
        b = valueOf(11, 31);
        c = a.multiply(b);
        assertEquals(valueOf(13, 31), c);

        a = valueOf(4, 31);
        b = valueOf(11, 31);
        c = a.power(BigInteger.valueOf(-4)).multiply(b);
        assertEquals(valueOf(13, 31), c);

        a = valueOf(12, 97);
        b = valueOf(77, 97);
        c = a.power(BigInteger.valueOf(7)).multiply(b.power(BigInteger.valueOf(49)));
        assertEquals(valueOf(63, 97), c);
    }

    @Test
    public void testDivide() {
        Field a = valueOf(3, 31);
        Field b = valueOf(24, 31);
        Field result = a.divide(b);
        assertEquals(valueOf(4, 31), result);

        a = valueOf(1, 5);
        b = valueOf(2, 5);
        result = a.divide(b);
        assertEquals(valueOf(3, 5), result);

        a = valueOf(2, 19);
        b = valueOf(7, 19);
        result = a.divide(b);
        assertEquals(valueOf(3, 19), result);

        a = valueOf(7, 19);
        b = valueOf(5, 19);
        result = a.divide(b);
        assertEquals(valueOf(9, 19), result);

        a = valueOf(7, 19);
        b = valueOf(5, 19);
        result = a.divide(b);
        assertEquals(valueOf(9, 19), result);
    }
}
