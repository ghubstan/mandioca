package mandioca.bitcoin.script.processing;

public class StackFullException extends IllegalStateException {
    public StackFullException(String s) {
        super(s);
    }
}
