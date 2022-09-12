package mandioca.bitcoin.rpc.response;

import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import mandioca.bitcoin.function.ThrowingBiFunction;
import mandioca.bitcoin.function.ThrowingFunction;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonResponseFunctions {

    public static final ThrowingBiFunction<Class<?>, JsonPrimitive, Object> toNumericGsonResponse = (c, p) -> {
        Number n = p.getAsNumber();
        if (n instanceof LazilyParsedNumber) {
            Constructor<?> ctor = c.getConstructors()[0];
            String typeName = ctor.getParameterTypes()[0].getName();
            if (typeName.equals("int")) {
                return c.getConstructor(int.class).newInstance(n.intValue());
            } else if (typeName.equals("double")) {
                return c.getConstructor(double.class).newInstance(n.doubleValue());
            } else if (typeName.equals("float")) {
                return c.getConstructor(float.class).newInstance(n.floatValue());
            } else if (typeName.equals("long")) {
                return c.getConstructor(long.class).newInstance(n.longValue());
            } else if (typeName.equals("short")) {
                return c.getConstructor(int.class).newInstance(n.intValue());
            } else if (typeName.equals("byte")) {
                return c.getConstructor(byte.class).newInstance(n.byteValue());
            } else {
                throw new RuntimeException("JsonResponseFunction does not support JsonPrimitive's LazilyParsedNumber value of type for value " + n.toString());
            }
        } else if (n instanceof BigDecimal) {
            return c.getConstructor(BigDecimal.class).newInstance(p.getAsBigDecimal());
        } else if (n instanceof BigInteger) {
            return c.getConstructor(BigInteger.class).newInstance(p.getAsBigInteger());
        } else if (n instanceof Integer) {
            return c.getConstructor(Integer.class).newInstance(p.getAsInt());
        } else if (n instanceof Float) {
            return c.getConstructor(Float.class).newInstance(p.getAsFloat());
        } else if (n instanceof Double) {
            return c.getConstructor(Double.class).newInstance(p.getAsDouble());
        } else if (n instanceof Long) {
            return c.getConstructor(Long.class).newInstance(p.getAsLong());
        } else {
            throw new RuntimeException("JsonResponseFunction does not support JsonPrimitive's numeric value of type " + n.getClass().getSimpleName());
        }
    };

    // Function returns response class instance from a JsonPrimitive, using reflection.
    // Parameters (c, p):  c = json response class,  p json primitive
    public static final ThrowingBiFunction<Class<?>, JsonPrimitive, Object> toGsonResponse = (c, p) -> {
        if (p.isString()) {
            return c.getConstructor(String.class).newInstance(p.getAsString());
        } else if (p.isBoolean()) {
            return c.getConstructor(Boolean.class).newInstance(p.getAsBoolean());
        } else if (p.isNumber()) {
            return toNumericGsonResponse.apply(c, p);
        }
        throw new RuntimeException("JsonResponseFunction does not support JsonPrimitive value type " + p.getAsJsonObject());
    };

    public static final ThrowingFunction<Class<?>, BitcoindRpcResponse> createResponseInstance = (c) ->
            (BitcoindRpcResponse) c.getConstructor().newInstance();

}
