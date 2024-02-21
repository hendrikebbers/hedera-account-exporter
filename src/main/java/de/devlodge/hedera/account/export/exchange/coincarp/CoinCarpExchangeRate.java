package de.devlodge.hedera.account.export.exchange.coincarp;

public record CoinCarpExchangeRate(long timestamp, double openRate, double highRate, double lowRate, double closeRate) {
}
