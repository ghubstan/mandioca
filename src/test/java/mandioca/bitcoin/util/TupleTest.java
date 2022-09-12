package mandioca.bitcoin.util;

import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static mandioca.bitcoin.function.BigIntegerFunctions.wrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("rawtypes")
public class TupleTest {

    @Test
    public void testGetBigIntegerTupleList() {
        List<Tuple> tuples =
                Tuple.getList(
                        new Tuple<>(wrap.apply(192), wrap.apply(105)),
                        new Tuple<>(wrap.apply(17), wrap.apply(56)),
                        new Tuple<>(wrap.apply(1), wrap.apply(193)));
        assertEquals(3, tuples.size());
        tuples.forEach(p -> assertTrue(p.getX() instanceof BigInteger && p.getY() instanceof BigInteger));
    }
}
