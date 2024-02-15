package de.devlodge.hedera.account.export.utils;

public record ExchangePair(Currency from, Currency to) {
    @Override
    public String toString() {
        return "%s-%s".formatted(from, to);
    }
}
