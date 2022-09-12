package mandioca.bitcoin.rpc;

// See https://github.com/bitcoin/bitcoin/blob/master/doc/JSON-RPC-interface.md
// See https://github.com/bitcoin/bitcoin/blob/master/doc/REST-interface.md
// See https://en.bitcoin.it/wiki/API_reference_%28JSON-RPC%29#Java
//
// See https://github.com/Polve/bitcoin-rpc-client for some tips (have not looked yet)
// https://github.com/Polve/bitcoin-rpc-client/blob/master/src/main/java/wf/bitcoin/javabitcoindrpcclient/BitcoindRpcClient.java
//

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static mandioca.bitcoin.rpc.RpcHelper.*;

/**
 * Simple bitcoind rpc client
 * <p>
 * See bitcoind error codes at https://github.com/bitcoin/bitcoin/pull/1908
 * <p>
 * https://github.com/bitcoin/bitcoin/pull/1908/files
 * <p>
 * https://github.com/laanwj/bitcoin/blob/285746d3dbed46d0e444d7a907c08453d36d99cd/src/bitcoinrpc.h
 * <p>
 * See error codes defined in java rcp client at  https://github.com/Polve/bitcoin-rpc-client
 * <p>
 * Using Optional:  http://blog.codefx.org/techniques/intention-revealing-code-java-8-optional
 * https://www.baeldung.com/java-optional-throw-exception
 */
public class RpcClient {

    // TODO support regtest

    private static final Supplier<RuntimeException> rpcMethodNotFoundException = () -> new RuntimeException("No rpc method defined");
    private static final Supplier<RuntimeException> rpcResponseClassNotFoundException = () -> new RuntimeException("No rpc response class defined");
    private static final Consumer<RpcCommand> confirmMethodIsConfigured = (c) -> c.getMethod().orElseThrow(rpcMethodNotFoundException);

    private final String rpcServerUrl;
    private final int rpcPort;
    private final String rpcUser;
    private final String rpcPassword;
    private HttpURLConnection urlConnection;

    private final Function<RpcCommand, Optional<String>> processError = (rpcCommand) -> {
        Optional<String> rawError = getResponseError();
        rawError.ifPresent(rpcCommand::setError); // set in the cmd, to be checked during json deserialization
        return rawError;
    };

    public RpcClient(boolean testnet) {
        Map<String, String> configurationMap = getConnectionParameters(testnet);
        this.rpcServerUrl = configurationMap.get("url");
        this.rpcPort = Integer.parseInt(configurationMap.get("port"));
        this.rpcUser = configurationMap.get("user");
        this.rpcPassword = configurationMap.get("password");
        authenticate();
    }

    private void authenticate() {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(rpcUser, rpcPassword.toCharArray());
            }
        });
    }

    public Object runCommand(RpcCommand rpcCommand) {
        confirmMethodIsConfigured.accept(rpcCommand);
        Class<?> responseClass = rpcCommand.getResponseClass().orElseThrow(rpcResponseClassNotFoundException);
        String response = processRequest(rpcCommand);
        return rpcCommand.causedError.get()
                ? deserializeErrorResponsePayload.apply(response, responseClass)
                : deserializeResponsePayload.apply(response, responseClass);
    }

    private String processRequest(RpcCommand rpcCommand) {
        try {
            openConnection();
            writeRequest(serializeRequestPayload.apply(rpcCommand));
            Optional<String> errorResponse = processError.apply(rpcCommand);
            return errorResponse.orElseGet(this::getSuccessfulResponse);
        } finally {
            closeConnection();
        }
    }

    private void writeRequest(byte[] requestPayload) {
        if (urlConnection == null) {
            throw new RuntimeException("No url connection");
        }
        try (OutputStream out = urlConnection.getOutputStream()) {
            out.write(requestPayload);
        } catch (IOException e) {
            throw new RuntimeException("Error sending request payload to " + urlConnection.toString(), e);
        }
    }

    private Optional<String> getResponseError() {
        try {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                return Optional.empty();
            } else if (responseCode >= 400 && responseCode < 500) {
                return Optional.of("Rpc server sent http error code " + responseCode);
            } else if (responseCode == 500) {
                return Optional.of(get500ErrorResponse());
            } else {
                throw new RuntimeException("Rpc server sent unexpected http error code " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving http response code", e);
        }

    }

    // See error codes in PR https://github.com/bitcoin/bitcoin/pull/1908 and affected files
    private String get500ErrorResponse() {
        try {
            return processRawResponse(urlConnection.getErrorStream());
        } catch (Exception e) {
            throw new RuntimeException("Error processing rpc server error response", e);
        }
    }

    private String getSuccessfulResponse() {
        try {
            return processRawResponse(urlConnection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error processing rpc server response", e);
        }
    }

    private String processRawResponse(InputStream is) throws IOException {
        StringBuilder contentBuffer = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                contentBuffer.append(inputLine);
            }
            return contentBuffer.toString();
        }
    }

    private void openConnection() {
        URL url;
        try {
            url = new URL(rpcServerUrl + ":" + rpcPort);
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");  // assumes all bitcoind requests are POSTs
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.setUseCaches(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() {
        urlConnection.disconnect();
    }
}
