package mandioca.bitcoin.util;

import java.io.*;


public class SerializationUtils {

    // SEE https://www.whitebyte.info/programming/snippets/java-serialization-class is something better?
    public static byte[] serialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("error serializing object of type " + object.getClass().getName(), e);
        } finally {
            try {
                baos.close();
            } catch (IOException ignored) {
            }
        }
    }

    // SEE https://www.whitebyte.info/programming/snippets/java-serialization-class is something better?
    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bais);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("error deserializing byte array", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
