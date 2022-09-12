package mandioca.bitcoin.function;

import java.math.BigDecimal;
import java.util.function.Function;

public class CurrencyFunctions {

    public static final BigDecimal SATOSHI_MULTIPLICAND = new BigDecimal(100000000);

    public static final Function<String, Long> btcToSatoshis = (s) -> {
        BigDecimal btc = new BigDecimal(s);
        return btc.multiply(SATOSHI_MULTIPLICAND).longValue();
    };

}
