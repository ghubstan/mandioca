package mandioca.bitcoin.ecc;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static mandioca.bitcoin.function.BigIntegerFunctions.wrap;

public abstract class AbstractEllipticCurveTest {

    static final String CURVE_EQ = "y^2 = x^3 + ax + b";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TestName testName = new TestName();

    protected EllipticCurve getCurve(int a, int b, int p) {
        return new EllipticCurve(testName.getMethodName(), CURVE_EQ,
                wrap.apply(a), wrap.apply(b), wrap.apply(p),
                null, null, null);
    }

    protected Point getPoint(EllipticCurve ec, int x, int y) {
        return ec.getPoint(wrap.apply(x), wrap.apply(y));
    }
}
