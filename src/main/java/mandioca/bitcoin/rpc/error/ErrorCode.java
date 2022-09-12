package mandioca.bitcoin.rpc.error;

// Source:  https://github.com/laanwj/bitcoin/blob/285746d3dbed46d0e444d7a907c08453d36d99cd/src/bitcoinrpc.h
// Some of the error messages have changed
// See https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html

import mandioca.bitcoin.function.ThrowingFunction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.util.Map.entry;

public enum ErrorCode {
    // Standard JSON-RPC 2.0 errors
    RPC_INVALID_REQUEST(-32600, "Invalid request"),
    RPC_METHOD_NOT_FOUND(-32601, "method not found"),
    RPC_INVALID_PARAMS(-32602, "invalid request params"),
    RPC_INTERNAL_ERROR(-32603, "internal server error"),
    RPC_PARSE_ERROR(-32700, "Request parsing error"),

    // General application defined errors
    RPC_MISC_ERROR(-1, "std::exception thrown in command handling"),
    RPC_FORBIDDEN_BY_SAFE_MODE(-2, "Server is in safe mode, and command is not allowed in safe mode"),
    RPC_TYPE_ERROR(-3, "Unexpected type was passed as parameter"),
    RPC_INVALID_ADDRESS_OR_KEY(-5, "Invalid or non-wallet transaction id"),
    RPC_OUT_OF_MEMORY(-7, "Ran out of memory during operation"),
    RPC_INVALID_PARAMETER(-8, "Invalid, missing or duplicate parameter"),
    RPC_DATABASE_ERROR(-20, "Database error"),
    RPC_DESERIALIZATION_ERROR(-22, "Error parsing or validating structure in raw format"),

    // P2P client errors
    RPC_CLIENT_NOT_CONNECTED(-9, "Bitcoin is not connected"),
    RPC_CLIENT_IN_INITIAL_DOWNLOAD(-10, "Still downloading initial blocks"),

    // Wallet errors
    RPC_WALLET_ERROR(-4, "Unspecified problem with wallet (key not found etc.)"),
    RPC_WALLET_INSUFFICIENT_FUNDS(-6, "Not enough funds in wallet or account"),
    RPC_WALLET_INVALID_ACCOUNT_NAME(-11, "Invalid account name"),
    RPC_WALLET_KEYPOOL_RAN_OUT(-12, "Keypool ran out, call keypoolrefill first"),
    RPC_WALLET_UNLOCK_NEEDED(-13, "Enter the wallet passphrase with walletpassphrase first"),
    RPC_WALLET_PASSPHRASE_INCORRECT(-14, "The wallet passphrase entered was incorrect"),
    RPC_WALLET_WRONG_ENC_STATE(-15, "Command given in wrong wallet encryption state (encrypting an encrypted wallet etc.)"),
    RPC_WALLET_ENCRYPTION_FAILED(-16, "Failed to encrypt the wallet"),
    RPC_WALLET_ALREADY_UNLOCKED(-17, "Wallet is already unlocked");

    private static final Map<Integer, ErrorCode> LOOKUP_MAP = LookupMapBuilder.build();
    public static final Function<Integer, Boolean> isValidErrorCode = LOOKUP_MAP::containsKey;
    public static final ThrowingFunction<Integer, ErrorCode> getErrorCode = (code) -> {
        ErrorCode errorCode = LOOKUP_MAP.get(code);
        if (errorCode == null) {
            throw new UnsupportedRpcErrorCodeException("No rpc server error code exists for " + code);
        } else {
            return errorCode;
        }
    };

    private final int code;
    private final String description;

    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return this.code;
    }

    public String description() {
        return this.description;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "name=" + this.name() +
                "  code=" + code +
                ", description='" + description + '\'' +
                '}';
    }

    private static final class LookupMapBuilder {
        public static void main(String[] args) {
            final Map<Integer, ErrorCode> map = build();
            Stream.of(map.entrySet()).forEach(out::println);

            out.println("-5 is valid error code:  " + isValidErrorCode.apply(-5));
            out.println("+13 is valid error code:  " + isValidErrorCode.apply(13));
            out.println("-5 is " + getErrorCode.apply(-5).toString());
        }

        static Map<Integer, ErrorCode> build() {
            final Map<Integer, ErrorCode> lookupMap = new TreeMap<>();
            Map.Entry<Integer, ErrorCode>[] entries = createEntries();
            Arrays.stream(entries).forEach(e -> lookupMap.put(e.getKey(), e.getValue()));
            return Collections.unmodifiableMap(lookupMap);
        }

        static Map.Entry<Integer, ErrorCode>[] createEntries() {
            @SuppressWarnings("rawtypes")
            List<Map.Entry> codeAndEnumEntryList = Stream.of(ErrorCode.values())
                    .map(e -> entry(e.code, e)).collect(Collectors.toUnmodifiableList());
            //noinspection unchecked
            return codeAndEnumEntryList.toArray(new Map.Entry[0]);
        }
    }
}
