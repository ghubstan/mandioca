package mandioca.real;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static mandioca.real.RealNumberPoint.valueOf;
import static org.junit.Assert.*;

public class RealNumberPointTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testConstructorPointNotOnCurveException1() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("1, -2 is not on the real number curve");
        valueOf(1, -2, 5, 7);
    }

    @Test
    public void testConstructorPointNotOnCurveException2() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("5, 7 is not on the real number curve");
        valueOf(5, 7, 5, 7);
    }

    @Test
    public void testConstructorPointNotOnCurveException3() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("2, 4 is not on the real number curve");
        valueOf(2, 4, 5, 7);
    }

    @Test
    public void testConstructorPointIsOnCurve() {
        valueOf(-1, -1, 5, 7);
        valueOf(18, 77, 5, 7);
    }

    @Test
    public void testEquals() {
        RealNumberPoint a = valueOf(-1, -1, 5, 7);
        RealNumberPoint b = valueOf(-1, -1, 5, 7);
        assertEquals(a, b);
        //noinspection SimplifiableJUnitAssertion
        assertTrue(a.equals(b));
    }

    @Test
    public void testNotEquals() {
        RealNumberPoint a = valueOf(-1, -1, 5, 7);
        RealNumberPoint b = valueOf(-1, 1, 5, 7);
        assertNotEquals(a, b);
        assertTrue(a.notEquals(b));
    }

    @Test
    public void testPointAddP2InfinityAdditiveProperty() {
        RealNumberPoint infinity = valueOf(5, 7);
        RealNumberPoint p = valueOf(-1, 1, 5, 7);
        RealNumberPoint result = infinity.add(p);
        assertEquals(p, result);
    }

    @Test
    public void testPointAddInfinity2PAdditiveProperty() {
        RealNumberPoint p = valueOf(-1, 1, 5, 7);
        RealNumberPoint infinity = valueOf(5, 7);
        RealNumberPoint result = p.add(infinity);
        assertEquals(p, result);
    }

    @Test
    public void testPointAdditionInvertibilityProperty() {
        RealNumberPoint p1 = valueOf(-1, -1, 5, 7);
        RealNumberPoint p2 = valueOf(-1, 1, 5, 7); // same x1=x2, y1!=y2 -> vertical line
        RealNumberPoint infinity = valueOf(5, 7);
        RealNumberPoint result = p1.add(p2);
        assertEquals(infinity, result);
    }

    @Test
    public void testPointAdditionForDifferentX() {
        RealNumberPoint p1 = valueOf(3, 7, 5, 7);
        RealNumberPoint p2 = valueOf(-1, -1, 5, 7); // x1 != x2
        RealNumberPoint result = p1.add(p2);
        assertEquals(valueOf(2, -5, 5, 7), result);
    }

    @Test
    public void testPointAdditionForDifferentXY() {
        RealNumberPoint p1 = valueOf(2, 5, 5, 7);
        RealNumberPoint p2 = valueOf(-1, -1, 5, 7);
        RealNumberPoint result = p1.add(p2);
        assertEquals(valueOf(3, -7, 5, 7), result);
    }


    @Ignore // TODO find pt on curve where y = 0
    @Test
    public void testPointAdditionForVerticalTangentLine() {
        try {
            RealNumberPoint p1 = valueOf(17, 64, 0, 7);
            RealNumberPoint p2 = p1.clone();
            RealNumberPoint result = p1.add(p2);
            // assertEquals(new RealNumberPoint(18, 77, 5, 7), result);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPointAdditionForIdenticalPoints() {
        try {
            RealNumberPoint p1 = valueOf(-1, -1, 5, 7);
            RealNumberPoint p2 = p1.clone();
            RealNumberPoint result = p1.add(p2);
            assertEquals(valueOf(18, 77, 5, 7), result);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            fail();
        }
    }
}
