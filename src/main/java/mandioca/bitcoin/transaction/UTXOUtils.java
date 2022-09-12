package mandioca.bitcoin.transaction;

import mandioca.bitcoin.address.Address;
import mandioca.bitcoin.function.TriFunction;
import mandioca.bitcoin.util.Triple;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static mandioca.bitcoin.function.CurrencyFunctions.SATOSHI_MULTIPLICAND;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;

public class UTXOUtils {

    private static final String TESTNET_PAYTXFEE = "0.0000105"; // bitcoin.conf paytxfee // TODO set up properties for TESTNET, MAINNET paytxfee

    private static final BigDecimal KB = new BigDecimal("1024");

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    private static final BigDecimal payTxFeePerByte = new BigDecimal(TESTNET_PAYTXFEE).divide(KB);

    public static final Function<Long, Long> calcLowestFee = (numBytes) -> {
        BigDecimal lowestFee = payTxFeePerByte
                .multiply(BigDecimal.valueOf(numBytes))
                .multiply(SATOSHI_MULTIPLICAND);
        if (lowestFee.compareTo(BigDecimal.valueOf(100L)) < 0) {
            return 100L;
        } else {
            return lowestFee.longValue();
        }
    };

    public static final Function<TxOut[], Long> calcAvailableBalance = (txOuts) ->
            (Long) stream(txOuts).map(TxOut::getAmountAsLong).mapToLong(Long::longValue).sum();

    public static final BiFunction<Long, Long, Long> calcTargetAmount = (balance, fee) -> balance - fee; // no change

    public static final TriFunction<TxOut[], Long, Long, Boolean> isDoubleSpend = (txOuts, targetAmt, feeAmt) -> {
        long availableBalance = calcAvailableBalance.apply(txOuts);
        return availableBalance - targetAmt - feeAmt < 0L;
    };

    public static final TriFunction<TxOut[], Long, Long, Boolean> leavesChange = (txOuts, targetAmt, feeAmt) -> {
        long availableBalance = calcAvailableBalance.apply(txOuts);
        return availableBalance - targetAmt - feeAmt > 0L;
    };

    public static final BiFunction<TxOut, String, Boolean> isFundingTxOut = (txOut, address) -> {
        Address txOutAddress = txOut.getScriptPubKey().address(NETWORK);
        return address.equals(txOutAddress.value());
    };

    public static Map<String, List<Triple<TxOut, Integer, Long>>> getUTXOsForAddressMap(List<Tx> transactions, String address) {
        Map<String, List<Triple<TxOut, Integer, Long>>> map = new LinkedHashMap<>();
        List<Map.Entry<String, List<Triple<TxOut, Integer, Long>>>> entries = new ArrayList<>();
        for (Tx tx : transactions) {
            String id = tx.id();
            TxOut[] txOuts = tx.getDeserializedOutputs();
            final List<Triple<TxOut, Integer, Long>> relevantTxOuts = new ArrayList<>();
            for (int i = 0; i < txOuts.length; i++) {
                TxOut txOut = txOuts[i];
                if (isFundingTxOut.apply(txOut, address)) {
                    relevantTxOuts.add(new Triple<>(txOut, i, txOut.getAmountAsLong()));
                }
            }
            entries.add(new AbstractMap.SimpleImmutableEntry(id, relevantTxOuts));
        }
        entries.stream().forEachOrdered(e -> map.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(map);
    }
}
