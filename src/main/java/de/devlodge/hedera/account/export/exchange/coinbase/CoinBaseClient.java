package de.devlodge.hedera.account.export.exchange.coinbase;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.utils.HttpUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CoinBaseClient implements ExchangeClient {

    private static final String BASE_URL = "https://api.coinbase.com/";
    private static final String PRICES_URL = BASE_URL + "v2/prices/%s/spot?date=%s";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String TIMEZONE_DE = "Europe/Berlin";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD).withZone(ZoneId.of(
            TIMEZONE_DE));

    private final HttpClient client;

    private final List<Exchange> exchanges = new CopyOnWriteArrayList<>();

    public CoinBaseClient() {
        client = HttpClient.newHttpClient();
    }

    public BigDecimal getExchangeRate(final ExchangePair pair, final Instant date) {
        Objects.requireNonNull(pair, "pair must not be null");
        Objects.requireNonNull(date, "date must not be null");

        LocalDate dateToUse = ZonedDateTime.ofInstant(date, ZoneId.of(TIMEZONE_DE)).toLocalDate();
        final Exchange exchange = exchanges.stream()
                .filter(e -> e.date().equals(dateToUse) && e.pair().equals(pair))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        final String url = String.format(PRICES_URL, pair, DATE_FORMATTER.format(date));
                        final BigDecimal rate = HttpUtils.getJsonElementForGetRequest(client, url)
                                .getAsJsonObject()
                                .getAsJsonObject("data")
                                .getAsJsonPrimitive("amount")
                                .getAsBigDecimal();
                        final Exchange e = new Exchange(dateToUse, pair, rate);
                        exchanges.add(e);
                        return e;
                    } catch (Exception e) {
                        throw new RuntimeException("Error in fetching exchange", e);
                    }
                });
        return exchange.rate();
    }

    private record Exchange(LocalDate date, ExchangePair pair, BigDecimal rate) {
    }

}
