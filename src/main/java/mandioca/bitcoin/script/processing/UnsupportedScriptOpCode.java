package mandioca.bitcoin.script.processing;

public class UnsupportedScriptOpCode extends IllegalArgumentException {
    public UnsupportedScriptOpCode(String s) {
        super(s);
    }
}
