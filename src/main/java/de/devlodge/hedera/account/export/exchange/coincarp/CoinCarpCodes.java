package de.devlodge.hedera.account.export.exchange.coincarp;

public enum CoinCarpCodes {
    HBAR("hederahashgraph"),
    EUR("stasis-eurs");

    private final String code;

    CoinCarpCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
