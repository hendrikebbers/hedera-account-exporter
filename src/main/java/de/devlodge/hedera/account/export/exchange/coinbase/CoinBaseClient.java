package de.devlodge.hedera.account.export.exchange.coinbase;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.utils.HttpUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CoinBaseClient implements ExchangeClient {

    private static final String BASE_URL = "https://api.coinbase.com/";
    private static final String PRICES_URL = BASE_URL + "v2/prices/%s/spot?date=%s";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Europe/Berlin"));

    private final HttpClient client;

    public CoinBaseClient() {
        client = HttpClient.newHttpClient();
    }

    public BigDecimal getExchangeRate(final ExchangePair pair, final Instant date)
            throws IOException, InterruptedException {
        Objects.requireNonNull(pair, "pair must not be null");
        Objects.requireNonNull(date, "date must not be null");

        final String url = String.format(PRICES_URL, pair, DATE_FORMATTER.format(date));
        return HttpUtils.getJsonElementForGetRequest(client, url)
                .getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonPrimitive("amount")
                .getAsBigDecimal();
    }

}
