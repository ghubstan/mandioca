package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

import static mandioca.bitcoin.address.AddressType.P2SH;

/**
 * P2SH addresses were created with the motivation of moving "the responsibility for supplying the conditions to
 * redeem a transaction from the sender of the funds to the redeemer. They allow the sender to fund an arbitrary
 * transaction, no matter how complicated, using a 20-byte hash"1. Pay-to-Pubkey-hash addresses are similarly a 20-byte hash of the public key.
 * <p>
 * Pay-to-script-hash provides a means for complicated transactions, unlike the Pay-to-pubkey-hash, which has
 * a specific definition for scriptPubKey, and scriptSig. The specification places no limitations on the script, and hence absolutely any contract can be funded using these addresses.
 * <p>
 * The scriptPubKey in the funding transaction is script which ensures that the script supplied in the redeeming
 * transaction hashes to the script used to create the address.
 * <p>
 * In the scriptSig above, 'signatures' refers to any script which is sufficient to satisfy the following serialized script.
 * <p>
 * scriptPubKey: OP_HASH160 <scriptHash> OP_EQUAL
 * scriptSig: ..signatures... <serialized script>
 * <p>
 * m-of-n multi-signature transaction:
 * scriptSig: 0 <sig1> ... <script>
 * script: OP_m <pubKey1> ... OP_n OP_CHECKMULTISIG
 *
 * @see {@link https://en.bitcoinwiki.org/wiki/Transaction_confirmation#Pay-to-Script-Hash}
 */
public class Pay2ScriptHashAddress extends AbstractAddress implements Address {

    public Pay2ScriptHashAddress(NetworkType networkType, String value) {
        super(P2SH, networkType, value);
    }
}
