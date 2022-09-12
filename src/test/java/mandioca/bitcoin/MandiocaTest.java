package mandioca.bitcoin;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.out;
import static mandioca.bitcoin.function.BitSetFunctions.setBitsToString;
import static mandioca.bitcoin.function.ByteArrayFunctions.*;
import static mandioca.bitcoin.util.HexUtils.HEX;

public class MandiocaTest {

    protected static final byte[] B0x0ff = new byte[]{(byte) MASK_0xFF};
    protected static final byte[] B0x00 = new byte[]{(byte) 0x00};
    protected static final byte[] B0x01 = new byte[]{(byte) 0x01};
    protected static final byte[] B0x02 = new byte[]{(byte) 0x02};
    protected static final byte[] B0x03 = new byte[]{(byte) 0x03};
    protected static final byte[] B0x04 = new byte[]{(byte) 0x04};
    protected static final byte[] B0x05 = new byte[]{(byte) 0x05};
    protected static final byte[] B0x06 = new byte[]{(byte) 0x06};
    protected static final byte[] B0x07 = new byte[]{(byte) 0x07};
    protected static final byte[] B0x08 = new byte[]{(byte) 0x08};
    protected static final byte[] B0x09 = new byte[]{(byte) 0x09};
    protected static final byte[] B0x0a = new byte[]{(byte) 0x0a};
    protected static final byte[] B0x0b = new byte[]{(byte) 0x0b};
    protected static final byte[] B0x0c = new byte[]{(byte) 0x0c};
    protected static final byte[] B0x0d = new byte[]{(byte) 0x0d};
    protected static final byte[] B0x0e = new byte[]{(byte) 0x0e};
    protected static final byte[] B0x0f = new byte[]{(byte) 0x0f};
    protected static final byte[] B0x10 = new byte[]{(byte) 0x10};

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TestName testName = new TestName();

    protected final Function<byte[], ByteArrayInputStream> stream = toByteArrayInputStream;
    protected final Function<String, ByteArrayInputStream> hexStream = hexToByteArrayInputStream;
    protected final Function<byte[], String> hexString = HEX::encode;

    @SuppressWarnings("unused")
    protected static void printSetBits(String msg, byte[] bytes) {
        out.println(msg + setBitsToString.apply(bytes));
    }

    // TODO move this to tx pkg if tx test are only ones that use this
    protected static Map<String, String> localTxCache;

    // TODO move this to tx pkg if tx test are only ones that use this
    @BeforeClass
    public static void loadLocalTxCache() {
        if (localTxCache == null) {
            localTxCache = loadTxCache();
        }
    }

    /**
     * Loads tx cache file in test resources folder into immutable map.
     * <p>
     * Expects copy of # Tx cache in programmingbitcoin/tx.cache without the (json) curly braces at 1st & last lines.
     *
     * @return
     */
    protected static Map<String, String> loadTxCache() {
        // TODO move this to tx pkg if tx test are only ones that use this
        final Map<String, String> cache = new TreeMap<>();
        //noinspection rawtypes
        final List<Map.Entry> cacheEntries = new ArrayList<>(17);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("pb-book-tx.cache");
        if (url == null) {
            throw new RuntimeException("Could not read tx cache file pb-book-tx.cache from test resources folder");
        }
        try (Scanner scanner = new Scanner(new File(url.getPath()))) {
            while (scanner.hasNext()) {
                String[] kv = scanner.nextLine().split(":");
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").replace(",", "").trim();
                cacheEntries.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error loading tx cache from disk", e);
        }
        //noinspection unchecked
        Map.Entry<String, String>[] entries = cacheEntries.toArray(new Map.Entry[0]);
        Arrays.stream(entries).forEach(e -> cache.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(cache);
    }
}
