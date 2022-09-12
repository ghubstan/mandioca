package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

abstract class AbstractAddress implements Address {

    protected final AddressType addressType;
    protected final NetworkType networkType;
    protected final String value;

    public AbstractAddress(AddressType addressType, NetworkType networkType, String value) {
        this.addressType = addressType;
        this.networkType = networkType;
        this.value = value;
    }

    @Override
    public AddressType addressType() {
        return this.addressType;
    }

    @Override
    public NetworkType networkType() {
        return this.networkType;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public boolean validate() {
        return addressType.validator.validate(networkType, value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "value='" + value + '\'' + "\n" +
                ", addressType=" + addressType + "\n" +
                ", networkType=" + networkType +
                '}';
    }
}
