package mandioca.bitcoin.network.block;

import mandioca.bitcoin.address.Address;
import mandioca.bitcoin.network.message.GetDataMessage;
import mandioca.bitcoin.network.message.MerkleBlockMessage;
import mandioca.bitcoin.network.node.FilterLoadClient;
import mandioca.bitcoin.network.node.GetDataClient;
import mandioca.bitcoin.network.node.GetHeadersClient;
import mandioca.bitcoin.network.node.HandshakeClient;
import mandioca.bitcoin.transaction.Tx;
import mandioca.bitcoin.transaction.TxOut;
import mandioca.bitcoin.util.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static mandioca.bitcoin.network.NetworkProperties.*;
import static mandioca.bitcoin.network.message.InventoryObjectType.MSG_FILTERED_BLOCK;
import static mandioca.bitcoin.network.node.SocketChannelFunctions.addressInfo;

public class BloomFilterQuery {

    // TODO inject nodeName ?

    private static final Logger log = LoggerFactory.getLogger(BloomFilterQuery.class);

    private final List<MerkleBlockMessage> merkleBlocks = new ArrayList<>();
    private final List<Tx> transactions = new ArrayList<>();
    private final List<Tx> matchingTransactions = new ArrayList<>();
    private final InetSocketAddress peer = new InetSocketAddress(LOCALHOST, BITCOIND_PORT.get());

    private FilterLoadClient filterLoadClient;
    private GetDataClient getDataClient;

    private final String[] addresses;
    private final BloomFilter bloomFilter;
    private final HandshakeClient handshakeClient;

    public BloomFilterQuery(String[] addresses, BloomFilter bloomFilter) {
        this.addresses = addresses;
        this.bloomFilter = bloomFilter;
        this.handshakeClient = new HandshakeClient("BloomFilterQuery", peer, false); // relay = 0
    }

    public boolean runQuery(String blockLocator) throws Exception {
        boolean gotResults = sendQuery(blockLocator);
        if (gotResults) {
            processResults();
        } else {
            log.info("no query results");
        }
        return gotResults;
    }

    public List<MerkleBlockMessage> getMerkleBlocks() {
        return merkleBlocks;
    }

    public List<Tx> getAllTransactions() {
        return transactions;
    }

    public List<Tx> getMatchingTransactions() {
        return matchingTransactions;
    }

    private boolean sendQuery(String blockLocator) throws Exception {
        doHandshake();
        prepareFilter();
        sendFilter();
        return getData(blockLocator);
    }

    private void processResults() {
        merkleBlocks.addAll(getDataClient.getMerkleBlocks());
        checkProofOfWork();
        transactions.addAll(getDataClient.getTransactions());
        cacheMatchingTransactions();
    }

    private void prepareFilter() {
        for (String address : addresses) {
            byte[] h160 = Base58.decodeChecked(address, true);
            bloomFilter.add(h160);
        }
    }

    private void doHandshake() throws Exception {
        boolean didHandshake = handshakeClient.call();
        if (!didHandshake) {
            throw new RuntimeException("unsuccessful handshake with " + addressInfo.apply(peer));
        }
    }

    private void sendFilter() throws IOException {
        filterLoadClient = new FilterLoadClient(
                "BloomFilterQuery",
                handshakeClient.getSocketChannel(),
                bloomFilter);
        boolean sentFilterLoad = filterLoadClient.call();
        if (!sentFilterLoad) {
            throw new RuntimeException("error sending filter to " + addressInfo.apply(peer));
        }
    }

    private boolean getData(String blockLocator) throws IOException {
        GetDataMessage getDataMessage = createGetDataMessage(blockLocator);
        getDataClient = new GetDataClient("BloomFilterQuery",
                filterLoadClient.getSocketChannel(),
                1024 * 1024 * 10,
                getDataMessage);
        return getDataClient.call();
    }

    private GetDataMessage createGetDataMessage(String blockLocator) throws IOException {
        List<BlockHeader> blockHeaders = getBlockHeaders(blockLocator);
        GetDataMessage getDataMessage = new GetDataMessage();
        for (BlockHeader blockHeader : blockHeaders) {
            new Block(blockHeader).checkProofOfWork();
            getDataMessage.add(MSG_FILTERED_BLOCK, blockHeader.hash());
        }
        return getDataMessage;
    }

    private List<BlockHeader> getBlockHeaders(String blockLocator) throws IOException {
        GetHeadersClient getHeadersClient = new GetHeadersClient(
                "BloomFilterQuery",
                handshakeClient.getSocketChannel(),
                1024 * 1024 * 10, // 10 MB, but there's a max limit that's much smaller (todo)
                blockLocator,
                1);
        return getHeadersClient.call();
    }

    private void checkProofOfWork() {
        for (MerkleBlockMessage merkleBlock : merkleBlocks) {
            if (!merkleBlock.isValid()) {
                throw new RuntimeException("invalid merkle proof");
            }
        }
    }

    private void cacheMatchingTransactions() {
        for (Tx transaction : transactions) {
            TxOut[] txOuts = transaction.getDeserializedOutputs();
            for (TxOut txOut : txOuts) {
                // TODO cache the txOut.index too, and test (use a map to hold all results, or just the matching txs?)
                for (String address : addresses) {
                    Address txOutAddress = txOut.getScriptPubKey().address(NETWORK);
                    if (txOutAddress == null) {
                        continue;
                    }
                    if (txOut.getScriptPubKey().address(NETWORK).value().equals(address)) {
                        matchingTransactions.add(transaction);
                    }
                }
            }
        }
    }
}
