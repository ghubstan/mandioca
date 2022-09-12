package mandioca.bitcoin.script.processing;

// See https://en.bitcoin.it/wiki/Script

import mandioca.bitcoin.ecc.Secp256k1Point;
import mandioca.bitcoin.ecc.Signature;
import mandioca.bitcoin.stack.Stack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.System.arraycopy;
import static mandioca.bitcoin.function.HashFunctions.*;
import static mandioca.bitcoin.script.processing.FlowControlOpFunctions.opVerify;
import static mandioca.bitcoin.stack.Stack.stackIsNotEmpty;

// See https://en.bitcoin.it/wiki/Script

class CryptoOpFunctions extends AbstractOpFunctions {

    static final Function<Stack, Boolean> opRipemd160 = (s) -> {
        // The input is hashed using RIPEMD-160
        // def op_ripemd160(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = stack.pop()
        //    stack.append(hashlib.new('ripemd160', element).digest())
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            s.push(hashRipemd160.apply(s.pop()));
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opSha1 = (s) -> {
        // The input is hashed using SHA-1
        // def op_sha1(stack):
        //    if len(stack) < 1:
        //        return False
        //    element = stack.pop()
        //    stack.append(hashlib.sha1(element).digest())
        //    return True
        if (stackIsNotEmpty.apply(s)) {
            s.push(sha1Hash.apply(s.pop()));
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opSha256 = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            s.push(sha256Hash.apply(s.pop()));
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opHash256 = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            s.push(hash256.apply(s.pop()));
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opHash160 = (s) -> {
        if (stackIsNotEmpty.apply(s)) {
            // there's at least 1 element on the stack
            byte[] hash = hash160.apply(s.pop());
            s.push(hash);
            return true;
        }
        return false;
    };

    static final Function<Stack, Boolean> opCodeSeparator = (s) -> {
        // All of the signature checking words will only match signatures to the
        // data after the most recently-executed OP_CODESEPARATOR.
        // No J Song Impl?
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };

    static final BiFunction<Stack, BigInteger, Boolean> opCheckSig = (s, z) -> {
        // The entire transaction's outputs, inputs, and script (from the most recently-executed OP_CODESEPARATOR to
        // the end) are hashed. The signature used by OP_CHECKSIG must be a valid signature for this hash and public key.
        // If it is, 1 is returned, 0 otherwise.
        // def op_checksig(stack, z):
        //    # check that there are at least 2 elements on the stack
        //    # the top element of the stack is the SEC pubkey
        //    # the next element of the stack is the DER signature
        //    # take off the last byte of the signature as that's the hash_type
        //    # parse the serialized pubkey and signature into objects
        //    # verify the signature using S256Point.verify()
        //    # push an encoded 1 or 0 depending on whether the signature verified
        // def op_checksig(stack, z):
        //    if len(stack) < 2:
        //        return False
        //    sec_pubkey = stack.pop()
        //    der_signature = stack.pop()[:-1]
        //    try:
        //        point = S256Point.parse(sec_pubkey)
        //        sig = Signature.parse(der_signature)
        //    except (ValueError, SyntaxError) as e:
        //        return False
        //    if point.verify(z, sig):
        //        stack.append(encode_num(1))
        //    else:
        //        stack.append(encode_num(0))
        //
        // See https://en.bitcoin.it/wiki/OP_CHECKSIG
        if (stackIsNotEmpty.apply(s) && s.size() >= 2) {
            try {
                byte[] sec = s.pop();                   // top element of the stack is the SEC pubkey
                int derLength = s.peek().length - 1;    // next element of the stack is the DER signature
                byte[] der = Arrays.copyOfRange(s.pop(), 0, derLength); // ignored last byte of der (the hash_type)
                Secp256k1Point point = Secp256k1Point.parse(sec);   // parse the serialized pubkey
                Signature signature = Signature.parse(der);         // parse the serialized signature
                if (point.verify(z, signature)) {
                    s.push(ENC_1);
                } else {
                    s.push(ENC_0);
                }
                return true;
            } catch (Exception e) {
                throw new RuntimeException("Error executing OP_CHECKSIG function", e);
            }
        }
        return false;
    };

    static final BiFunction<Stack, BigInteger, Boolean> opCheckSigVerify = (s, z) -> {
        // Same as OP_CHECKSIG, but OP_VERIFY is executed afterward
        // def op_checksigverify(stack, z):
        //    return op_checksig(stack, z) and op_verify(stack)
        // See https://en.bitcoin.it/wiki/OP_CHECKSIG
        return opCheckSig.apply(s, z) && opVerify.apply(s);
    };


    static final BiFunction<Stack, BigInteger, Boolean> opCheckMultiSig = (s, z) -> {
        // Compares the first signature against each public key until it finds an ECDSA match. Starting with the
        // subsequent public key, it compares the second signature against each remaining public key until it
        // finds an ECDSA match. The process is repeated until all signatures have been checked or not enough
        // public keys remain to produce a successful result. All signatures need to match a public key.
        // Because public keys are not checked again if they fail any signature comparison, signatures must be
        // placed in the scriptSig using the same order as their corresponding public keys were placed in the
        // scriptPubKey or redeemScript. If all signatures are valid, 1 is returned, 0 otherwise.
        // Due to OP_CHECKMULTISIG's off-by-one bug, one extra unused value is removed from the stack.
        if (stackIsNotEmpty.apply(s)) {
            int n = decodeElement(s.pop());
            if (s.size() < n + 1) {
                return false;
            }
            final byte[][] secPubKeys = new byte[n][];
            for (int i = 0; i < n; i++) {
                secPubKeys[i] = s.pop();
            }

            int m = decodeElement(s.pop());
            if (s.size() < m + 1) {
                return false;
            }
            final byte[][] derSignatures = new byte[m][];
            for (int i = 0; i < m; i++) {
                byte[] der = new byte[s.peek().length - 1];  // signature is assumed to be using SIGHASH_ALL
                arraycopy(s.pop(), 0, der, 0, der.length);
                derSignatures[i] = der;
            }

            // pop OP_CHECKMULTISIG bug element 0, and check value = 0
            if (!isEncodedZero.test(s.pop())) {
                System.err.println("OP_CHECKMULTISIG off-by-one bug element != zero element byte array");
                return false;
            }

            try {
                List<Secp256k1Point> points = new ArrayList<>(n);
                Stream.of(secPubKeys).forEachOrdered(sec -> points.add(Secp256k1Point.parse(sec)));
                List<Signature> signatures = new ArrayList<>(m);
                Stream.of(derSignatures).forEachOrdered(der -> signatures.add(Signature.parse(der)));
                for (Signature signature : signatures) {
                    if (points.isEmpty()) {
                        System.err.println("OP_CHECKMULTISIG signatures no good or not in right order");
                        return false;
                    }
                    for (int i = 0; i < m; i++) {  // find point which works with this signature
                        Secp256k1Point point = points.remove(0);
                        if (point.verify(z, signature)) {
                            break; // found it, move on to the next signature
                        }
                    }
                }

                s.push(ENC_1);     // signatures are valid, push 1 on the stack

            } catch (Exception e) {
                System.err.println("OP_CHECKMULTISIG failed to match points with signatures");
                e.printStackTrace();
                return false;
            }

            return true;
        }
        return false;
    };

    static final BiFunction<Stack, BigInteger, Boolean> opCheckMultiSigVerify = (s, z) -> {
        // Same as OP_CHECKMULTISIG, but OP_VERIFY is executed afterward.
        // def op_checkmultisigverify(stack, z):
        //    return op_checkmultisig(stack, z) and op_verify(stack)
        if (stackIsNotEmpty.apply(s)) {
            throw new RuntimeException("TODO");
        }
        return false;
    };
}
