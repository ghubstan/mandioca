package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

public class AddressValidationError {
    private final NetworkType networkType;
    private final AddressType addressType;
    private final String address;
    private final String reason;

    public AddressValidationError(NetworkType networkType, AddressType addressType, String address, String reason) {
        this.networkType = networkType;
        this.addressType = addressType;
        this.address = address;
        this.reason = reason;
    }

    public String description() {
        String error = String.format("%s address '%s' is not a valid %s address because %s",
                networkType.name(), address, addressType.name(), reason);
        return error;
    }
}
