package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.function.ThrowingFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.util.Map.entry;
import static java.util.stream.Stream.of;
import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;
import static mandioca.bitcoin.function.ByteCompareFunctions.isEqual;
import static mandioca.bitcoin.util.HexUtils.HEX;

// See https://en.bitcoin.it/wiki/Script

public class Op {

    public static final BiFunction<Byte, OpCode, Boolean> isOpCode = (b, c) -> isEqual.apply(b, c.code);

    // TODO Remember that multiple OpCodes mapped to the same byte may become a problem.
    private static final Map<Byte, OpCode> OPCODE_LOOKUP_MAP = build();
    static final Predicate<Byte> isSupportedOpCode = OPCODE_LOOKUP_MAP::containsKey;


    public static final ThrowingFunction<Byte, OpCode> getOpCode = (b) -> {
        OpCode opCode = OPCODE_LOOKUP_MAP.get(b);
        if (opCode == null) {
            throw new UnsupportedScriptOpCode("Script OpCode " + HEX.byteToPrefixedHex.apply(b) + " is not supported");
        } else {
            return opCode;
        }
    };

    public static int getOpCodeAsInt(OpCode opCode) {
        return opCode.code & MASK_0xFF;
    }

    public static String getOpCodeName(byte b) {
        if (isSupportedOpCode.test(b)) {
            return getOpCode.apply(b).name();
        } else {
            throw new UnsupportedScriptOpCode("Script OpCode " + HEX.byteToPrefixedHex.apply(b) + " is not supported");
        }
    }

    public static String getOpCodeDescription(byte b) {
        if (isSupportedOpCode.test(b)) {
            return getOpCode.apply(b).description();
        } else {
            throw new UnsupportedScriptOpCode("Script OpCode " + HEX.byteToPrefixedHex.apply(b) + " is not supported");
        }
    }

    public static OpCode toOpCode(String name) {
        return Enum.valueOf(OpCode.class, name);
    }

    public static void dumpOpCodeLookupMap() {
        out.println("Map<Byte, Code>:");
        OPCODE_LOOKUP_MAP.forEach((k, v) -> out.printf("\t%20s %s\n", k, v));
    }

    /**
     * Returns a Map<Byte, OpCode> for looking up OpCode with code.
     *
     * @return Map<Byte, OpCode>
     */
    private static Map<Byte, OpCode> build() {
        // TODO Remember that multiple OpCodes mapped to the same byte may become a problem.
        final Map<Byte, OpCode> lookupMap = new TreeMap<>();
        List<Map.Entry<Byte, OpCode>> codeAndEnumEntryList = of(OpCode.values())
                .filter(v -> v.enabled) // exclude disabled opcodes from lookup map
                .map(e -> entry(e.code, e)).collect(Collectors.toUnmodifiableList());
        @SuppressWarnings("unchecked")
        Map.Entry<Byte, OpCode>[] entries = codeAndEnumEntryList.toArray(new Map.Entry[0]);
        Arrays.stream(entries).forEach(e -> lookupMap.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(lookupMap);
    }
}



