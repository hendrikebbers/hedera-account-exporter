package de.devlodge.hedera.account.export.model;

public enum Network {
    HEDERA_MAINNET("Hedera mainnet", Currency.HBAR);

    private final String readableName;

    private final Currency currency;

    Network(String readableName, final Currency currency) {
        this.readableName = readableName;
        this.currency = currency;
    }

    public String getReadableName() {
        return readableName;
    }

    public Currency getCurrency() {
        return currency;
    }
}
