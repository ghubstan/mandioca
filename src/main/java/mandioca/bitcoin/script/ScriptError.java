package mandioca.bitcoin.script;

import mandioca.bitcoin.script.processing.ScriptErrorCode;

public class ScriptError {

    private ScriptErrorCode scriptErrorCode;

    public ScriptError() {
    }

    public ScriptErrorCode getScriptErrorCode() {
        return scriptErrorCode;
    }

    public void setScriptErrorCode(ScriptErrorCode scriptErrorCode) {
        this.scriptErrorCode = scriptErrorCode;
    }
}
