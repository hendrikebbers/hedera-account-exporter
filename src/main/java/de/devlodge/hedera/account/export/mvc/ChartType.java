package de.devlodge.hedera.account.export.mvc;

public enum ChartType {
    EUR("eur", "Guthaben EUR"), HBAR("hbar", "Guthaben HBAR"), STACKING_HBAR("stakingHbar", "Staking HBAR"), STACKING_EUR("stakingEur", "Staking EUR"), EXCHANGE_RATE("exchange", "Wechselkurs");

    private final String value;

    private final String header;

    private ChartType(final String value, String header) {
        this.value = value;
        this.header = header;
    }

    public String getValue() {
        return value;
    }

    public String getHeader() {
        return header;
    }

    public static ChartType fromString(String text) {
        for (ChartType b : ChartType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
       throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
