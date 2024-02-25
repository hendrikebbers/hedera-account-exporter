package de.devlodge.hedera.account.export.model;

public enum Currency {
    HBAR("ℏ"),
    EUR("€"),
    USD("$");

    private final String sign;

    Currency(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }
}
