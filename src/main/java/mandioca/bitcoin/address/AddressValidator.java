package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

import java.util.Optional;

public interface AddressValidator {
    boolean validate(NetworkType networkType, String address);

    Optional<AddressValidationError> getError();
}
