package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

public interface Address {

    AddressType addressType();

    NetworkType networkType();

    String value();

    boolean validate();
}
