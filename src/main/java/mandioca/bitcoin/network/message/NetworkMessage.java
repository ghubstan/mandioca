package mandioca.bitcoin.network.message;

// See https://en.bitcoin.it/wiki/Protocol_documentation

public interface NetworkMessage {

    MessageType getMessageType();

    byte[] serialize();
}
