package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

import static mandioca.bitcoin.address.AddressType.P2PKH;

/**
 * A P2PKH address is only a hash, and senders can't provide a full public key in scriptPubKey. When redeeming coins
 * that have been sent to a Bitcoin address, the recipient provides both the signature and the public key.
 * The script verifies that the provided public key does hash to the hash in scriptPubKey, and then it also checks
 * the signature against the public key.
 * <p>
 * ScriptPubKey: OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
 * ScriptSig: <sig> <pubKey>
 *
 * @see {@link https://en.bitcoinwiki.org/wiki/Transaction_confirmation#Pay-to-PubkeyHash}
 */
public class Pay2PubKeyHashAddress extends AbstractAddress implements Address {

    public Pay2PubKeyHashAddress(NetworkType networkType, String value) {
        super(P2PKH, networkType, value);
    }
}
