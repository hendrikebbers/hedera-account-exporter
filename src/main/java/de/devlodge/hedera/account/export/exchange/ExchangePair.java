package de.devlodge.hedera.account.export.exchange;

import de.devlodge.hedera.account.export.model.Currency;

public record ExchangePair(Currency from, Currency to) {
    @Override
    public String toString() {
        return "%s-%s".formatted(from, to);
    }
}
