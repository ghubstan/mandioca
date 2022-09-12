package mandioca.bitcoin.transaction;

import mandioca.bitcoin.address.Address;
import mandioca.bitcoin.script.ScriptType;
import mandioca.bitcoin.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static mandioca.bitcoin.function.ByteArrayFunctions.intToBytes;
import static mandioca.bitcoin.function.ByteArrayFunctions.longToBytes;
import static mandioca.bitcoin.network.NetworkProperties.NETWORK;
import static mandioca.bitcoin.script.Script.StandardScripts.addressToP2pkhScript;
import static mandioca.bitcoin.transaction.TransactionFactory.TxOutFactory.createOutputs;
import static mandioca.bitcoin.util.HexUtils.HEX;

@SuppressWarnings("unused")
public final class TransactionFactory {

    private static final Logger log = LoggerFactory.getLogger(TransactionFactory.class);

    public static final BiFunction<String, Long, TxOut> createP2pkhScriptOutput = (targetAddress, targetAmount) ->
            new TxOut(longToBytes.apply(targetAmount), addressToP2pkhScript.apply(targetAddress));

    public static Tx createTransaction(
            TxIn[] txIns,
            String targetAddress,
            long targetAmount,
            long fee,
            ScriptType scriptType) {
        switch (scriptType) {
            case P2PKH:
                return new Tx(2, txIns,
                        new TxOut[]{createP2pkhScriptOutput.apply(targetAddress, targetAmount)},
                        0, NETWORK);
            case P2PK:
            case P2SH:
            case P2WPKH:
            case P2WSH:
            default:
                // TODO support "all" script types
                throw new RuntimeException("unsupported script type " + scriptType);
        }
    }

    public static Tx createTransaction(
            TxIn[] txIns,
            String targetAddress,
            long targetAmount,
            String changeAddress,
            long changeAmount,
            long fee,
            ScriptType scriptType) {
        TxOut[] txOuts = createOutputs(scriptType,
                new Tuple<>(targetAddress, targetAmount),
                new Tuple<>(changeAddress, changeAmount));
        return new Tx(2, txIns, txOuts, 0, NETWORK);
    }

    public static void printTxObject(Tx txObject, long targetAmount, long fee) {
        byte[] rawTx = txObject.serialize();
        String txInfo = String.format("Signed %s\nHex=%s\nSize=%d bytes\nTarget Amt=%d\nFee Amt=%d\n",
                txObject.toString(),
                HEX.encode(rawTx),
                rawTx.length,
                targetAmount,
                fee);
        log.info(txInfo);
    }

    static class TxInFactory {

        private static final BiFunction<TxOut, String, Boolean> isPreviousInputFromAddress = (txOut, address) -> {
            Address txOutAddress = txOut.getScriptPubKey().address(NETWORK);
            return address.equals(txOutAddress.value());
        };

        @SafeVarargs
        public static TxIn[] createInputs(Tuple<String /*prevTxId*/, Integer /*prevTxIdx*/>... prevTxIdAndIndex) {
            final TxIn[] txIns = new TxIn[prevTxIdAndIndex.length];
            for (int i = 0; i < prevTxIdAndIndex.length; i++) {
                txIns[i] = createInput(prevTxIdAndIndex[i]);
            }
            return txIns;
        }

        public static TxIn[] createInputs(String[] prevTxIds, int[] prevTxIndexes) {
            if (prevTxIds.length != prevTxIndexes.length) {
                throw new IllegalArgumentException("method requires equal number of input ids and indexes");
            }
            final TxIn[] txIns = new TxIn[prevTxIds.length];
            for (int i = 0; i < prevTxIds.length; i++) {
                txIns[i] = createInput(prevTxIds[i], prevTxIndexes[i]);
            }
            return txIns;
        }

        public static TxIn createInput(Tuple<String, Integer> prevTxIdAndIndex) {
            return new TxIn(HEX.decode(prevTxIdAndIndex.getX()), intToBytes.apply(prevTxIdAndIndex.getY()));
        }

        public static TxIn createInput(String prevTxIdHex, int prevTxIndex) {
            byte[] prevTxId = HEX.decode(prevTxIdHex);
            return new TxIn(prevTxId, intToBytes.apply(prevTxIndex));
        }

        @SafeVarargs
        public static List<Tuple<String, Integer>> getPrevInputTupleList(Tuple<String, Integer>... tuples) {
            final List<Tuple<String, Integer>> list = new ArrayList<>();
            Arrays.stream(tuples).forEach(t -> list.add(new Tuple<>((String) t.getX(), (int) t.getY())));
            return list;   // return Tuple<prevTxId, prevTxIndex>
        }
    }


    static class TxOutFactory {

        @SafeVarargs
        public static TxOut[] createOutputs(ScriptType scriptType, Tuple<String /*address*/, Long /*amt*/>... addressAndAmount) {
            final TxOut[] txOuts = new TxOut[addressAndAmount.length];
            for (int i = 0; i < addressAndAmount.length; i++) {
                txOuts[i] = createOutput(scriptType, addressAndAmount[i]);
            }
            return txOuts;
        }

        public static TxOut createOutput(ScriptType scriptType, Tuple<String, Long> addressAndAmount) {
            switch (scriptType) {
                case P2PKH:
                    return createP2pkhScriptOutput.apply(addressAndAmount.getX(), addressAndAmount.getY());
                case P2PK:
                case P2SH:
                case P2WPKH:
                case P2WSH:
                default:
                    // TODO support "all" script types
                    throw new RuntimeException("unsupported script type " + scriptType);
            }
        }

        public static TxOut createOutput(ScriptType scriptType, String address, long amount) {
            switch (scriptType) {
                case P2PKH:
                    return createP2pkhScriptOutput.apply(address, amount);
                case P2PK:
                case P2SH:
                case P2WPKH:
                case P2WSH:
                default:
                    // TODO support "all" script types
                    throw new RuntimeException("unsupported script type " + scriptType);
            }
        }

        @SafeVarargs
        public static List<Tuple<String, Long>> getOutputTupleList(Tuple<String, Long>... tuples) {
            final List<Tuple<String, Long>> list = new ArrayList<>();
            Arrays.stream(tuples).forEach(t -> list.add(new Tuple<>((String) t.getX(), (long) t.getY())));
            return list;   // return Tuple<address, amount>
        }

    }
}
