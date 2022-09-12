package mandioca.bitcoin.script.processing;

public class StackEmptyException extends IllegalStateException {
    public StackEmptyException(String s) {
        super(s);
    }
}
