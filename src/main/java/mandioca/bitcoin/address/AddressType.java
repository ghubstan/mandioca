package mandioca.bitcoin.address;

public enum AddressType {

    OP_RETURN_DATA("arbitrary bytes after OP_RETURN", new NullDataAddressValidator()),
    P2PKH("pay to pubkey hash", new Pay2PubKeyHashAddressValidator()),
    P2SH("pay to script hash", new Pay2ScriptHashAddressValidator());

    // TODO p2wpkh (validator), p2wsh (validator)

    final String description;
    final AddressValidator validator;

    AddressType(String description, AddressValidator validator) {
        this.description = description;
        this.validator = validator;
    }

    @Override
    public String toString() {
        return "AddressType{" + "description='" + description + '\''
                + ", validator=" + validator.getClass().getSimpleName() + '}';
    }
}
