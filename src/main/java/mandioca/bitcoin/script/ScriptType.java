package mandioca.bitcoin.script;

// Stolen from https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/script/Script.java

public enum ScriptType {

    // TODO add function parmaeter that converts address to script?

    P2PKH(1),       // pay to pubkey hash (aka pay to address)
    P2PK(2),        // pay to pubkey
    P2SH(3),        // pay to script hash
    P2WPKH(4),      // pay to witness pubkey hash
    P2WSH(5);       // pay to witness script hash

    public final int id;

    ScriptType(int id) {
        this.id = id;
    }
}
