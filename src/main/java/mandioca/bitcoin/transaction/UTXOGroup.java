package mandioca.bitcoin.transaction;

import mandioca.bitcoin.util.Triple;
import mandioca.bitcoin.util.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.transaction.UTXOUtils.getUTXOsForAddressMap;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * Convenient mapping of tx.id keys to lists of prevTxIndex, TxOut, & TxOut.amountAsLong,
 * for tx querying, creation and testing.
 */
@SuppressWarnings("SimplifyStreamApiCallChains")
public final class UTXOGroup {

    private final Map<String, List<Triple<TxOut, Integer, Long>>> utxoMap;

    public UTXOGroup() {
        this.utxoMap = new LinkedHashMap<>();
    }

    public void add(List<Tx> transactions, String address) {
        this.utxoMap.putAll(getUTXOsForAddressMap(transactions, address));
    }

    public int size() {
        return utxoMap.size();
    }

    public long totalAmount() {
        long[] amt = {0};
        utxoMap.entrySet().stream().forEachOrdered((entry) -> {
            // Triple<TxOut, prevTxIndex,  txOut.amountAsLong>
            List<Triple<TxOut, Integer, Long>> outInfo = entry.getValue();
            amt[0] += outInfo.stream().map(Triple::getZ).mapToLong(Long::longValue).sum();
        });
        return amt[0];
    }

    public TxIn[] txInputs() {
        List<TxIn> txInList = new ArrayList<>();
        utxoMap.entrySet().stream().forEachOrdered((entry) -> {
            // Triple<TxOut, prevTxIndex,  txOut.amountAsLong>
            List<Triple<TxOut, Integer, Long>> outInfo = entry.getValue();
            if (outInfo.size() > 1) {
                // TODO should I be using a List<triple> anyway?
                throw new RuntimeException("uh oh, assumption is list.size = 1 (and it should be a list if it is");
            }
            txInList.add(new TxIn(HEX.decode(entry.getKey()), intToBytes.apply(outInfo.get(0).getY())));
        });
        return txInList.toArray(new TxIn[0]);
    }

    public String[] previousTxIds() {
        List<String> prevTxIds = new ArrayList<>();
        utxoMap.entrySet().stream().forEachOrdered((entry) -> {
            prevTxIds.add(entry.getKey());
        });
        return prevTxIds.toArray(new String[0]);
    }

    public int[] previousTxIndexes() {
        List<Integer> prevIndexes = new ArrayList<>();
        utxoMap.entrySet().stream().forEachOrdered((entry) -> {
            // Triple<TxOut, prevTxIndex,  txOut.amountAsLong>
            List<Triple<TxOut, Integer, Long>> outInfo = entry.getValue();
            prevIndexes.addAll(outInfo.stream().map(Triple::getY).collect(Collectors.toList()));
        });
        return prevIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    public Tuple<String, Integer>[] previousTxIdAndIndexPairs() {
        List<Tuple<String, Integer>> tuples = new ArrayList<>();
        utxoMap.entrySet().stream().forEachOrdered((entry) -> {
            List<Triple<TxOut, Integer, Long>> outInfo = entry.getValue();
            if (outInfo.size() > 1) {
                // TODO should I be using a List<triple> anyway?
                throw new RuntimeException("uh oh, assumption is list.size = 1 (and it should be a list if it is");
            }
            tuples.add(new Tuple<>(entry.getKey(), outInfo.get(0).getY()));
        });
        return tuples.toArray(new Tuple[0]);
    }

    public TxOut[] txOutputs() {
        List<TxOut> txOutList = new ArrayList<>();
        utxoMap.entrySet().stream().forEachOrdered((entry) -> {
            // Triple<TxOut, prevTxIndex,  txOut.amountAsLong>
            List<Triple<TxOut, Integer, Long>> outInfo = entry.getValue();
            if (outInfo.size() > 1) {
                // TODO should I be using a List<triple> anyway?
                throw new RuntimeException("uh oh, assumption is list.size = 1 (and it should be a list if it is");
            }
            txOutList.addAll(outInfo.stream().map(Triple::getX).collect(Collectors.toList()));
        });
        return txOutList.toArray(new TxOut[0]);
    }

    public List<UTXO> getUTXOs() {
        final List<UTXO> utxos = new ArrayList<>();
        utxoMap.entrySet().stream().forEachOrdered((e) -> {
            // Triple<TxOut, prevTxIndex,  txOut.amountAsLong>
            List<Triple<TxOut, Integer, Long>> v = e.getValue();
            v.stream().forEachOrdered(outInfo ->
                    utxos.add(new UTXO(e.getKey(), outInfo.getY(), outInfo.getX(), outInfo.getZ())));
        });
        return utxos;
    }
}
