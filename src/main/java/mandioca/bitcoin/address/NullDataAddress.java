package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

import static mandioca.bitcoin.address.AddressType.OP_RETURN_DATA;

public class NullDataAddress extends AbstractAddress implements Address {

    public NullDataAddress(NetworkType networkType, String value) {
        super(OP_RETURN_DATA, networkType, value);
    }
}
