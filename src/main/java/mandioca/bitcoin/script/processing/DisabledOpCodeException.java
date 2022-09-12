package mandioca.bitcoin.script.processing;

public class DisabledOpCodeException extends IllegalStateException {
    public DisabledOpCodeException(String s) {
        super(s);
    }
}
