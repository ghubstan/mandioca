package mandioca.bitcoin.rpc;

import com.google.gson.*;
import mandioca.bitcoin.rpc.response.RpcErrorResponse;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

import static mandioca.bitcoin.rpc.response.JsonResponseFunctions.toGsonResponse;

// See https://github.com/google/gson/blob/master/UserGuide.md
// See https://github.com/google/gson/tree/master/gson/src/test/java/com/google/gson
// See https://www.javaguides.net/2018/10/gson-custom-serialization-and-deseriliazation-examples.html

class GsonHelper {

    private static final GsonBuilder BUILDER = new GsonBuilder();
    private final FieldNamingPolicy fieldNamingPolicy;
    private final boolean isPrettyPrinting;
    private final Function<Class<?>, Boolean> isErrorResponseClass = (c) -> c.getSimpleName().equals(RpcErrorResponse.class.getSimpleName());
    private Gson gson;

    GsonHelper(FieldNamingPolicy fieldNamingPolicy, @SuppressWarnings("SameParameterValue") boolean isPrettyPrinting) {
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.isPrettyPrinting = isPrettyPrinting;
        init();
    }

    private void init() {
        BUILDER.registerTypeAdapter(BigDecimal.class, new BigDecimalDeserializer());
        // TODO BUILDER.setDateFormat(String pattern)
        if (isPrettyPrinting) {
            BUILDER.setPrettyPrinting();
        }
        gson = new GsonBuilder().setFieldNamingPolicy(fieldNamingPolicy).create();
    }

    Object fromJson(String jsonString, Class<?> clazz) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            if (isErrorResponseClass.apply(clazz)) {
                return fromJson(jsonObject.get("error"), clazz);
            } else {
                // all bitcoind's json responses contain top level nodes result, error, id
                checkForRcpServerErrorMessage(jsonObject);
                checkForRcpServerResponseId(jsonObject);
                return fromJson(jsonObject.get("result"), clazz);
            }
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Error parsing rcp server's json response", e);
        }
    }

    private void checkForRcpServerErrorMessage(JsonObject jsonObject) {
        // See https://github.com/Polve/bitcoin-rpc-client/blob/master/src/main/java/wf/bitcoin/javabitcoindrpcclient/BitcoinRPCErrorCode.java
        // for error codes
        JsonElement error = jsonObject.get("error");
        if (!error.isJsonNull()) {
            throw new RuntimeException("Rcp response contained error message " + error.getAsString());
        }
    }

    private void checkForRcpServerResponseId(JsonObject jsonObject) {
        JsonElement id = jsonObject.get("id");
        if (id.isJsonNull()) {
            throw new RuntimeException("Bitcoind rcp server's response id is missing");
        }
    }

    private Object fromJson(JsonElement result, Class<?> clazz) {
        if (result != null) {
            if (result.isJsonPrimitive()) {
                return fromJsonPrimitive(result.getAsJsonPrimitive(), clazz);
            } else {
                return gson.fromJson(result, clazz);
            }
        } else {
            throw new RuntimeException("bitcoind rcp response result element is null ");
        }
    }


    String toJson(Object object) {
        return gson.toJson(object);
    }

    private Object fromJsonPrimitive(JsonPrimitive jsonPrimitive, Class<?> clazz) {
        try {
            if (jsonPrimitive.isJsonNull()) {
                throw new RuntimeException("error instantiating class constructor for " + clazz.getName() + " with null argument");
            } else {
                return toGsonResponse.apply(clazz, jsonPrimitive);
            }
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw new RuntimeException("error instantiating class constructor for " + clazz.getName(), e.getCause());
            } else {
                throw new RuntimeException("error instantiating class constructor for " + clazz.getName());
            }
        }
    }

    static class BigDecimalDeserializer implements JsonDeserializer<BigDecimal> {
        @Override
        public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new BigDecimal(json.getAsString());
        }
    }

    static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsString(),
                    DateTimeFormatter.ofPattern("d-MMM-yyyy").withLocale(Locale.ENGLISH));
        }
    }

    static class TimestampDeserializer implements JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return null; // TODO
        }
    }
}
