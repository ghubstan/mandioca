package mandioca.bitcoin.address;

import mandioca.bitcoin.network.NetworkType;

import java.util.Optional;

class Pay2ScriptHashAddressValidator implements AddressValidator {

    private Optional<AddressValidationError> validationError = Optional.empty();

    @Override
    public boolean validate(NetworkType networkType, String address) {
        validationError = Optional.empty();
        return true;
    }

    @Override
    public Optional<AddressValidationError> getError() {
        return validationError;
    }
}
