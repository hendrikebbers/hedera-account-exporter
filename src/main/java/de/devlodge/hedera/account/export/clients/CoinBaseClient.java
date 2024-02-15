package de.devlodge.hedera.account.export.clients;

import com.google.gson.JsonParser;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CoinBaseClient {

    private static final String BASE_URL = "https://api.coinbase.com/";
    private static final String PRICES_URL = BASE_URL + "v2/prices/%s/spot?date=%s";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Europe/Berlin"));

    private final HttpClient client;

    public CoinBaseClient() {
        client = HttpClient.newHttpClient();
    }

    public double getExchangeRate(final ExchangePair pair, final Instant date)
            throws IOException, InterruptedException {
        Objects.requireNonNull(pair, "pair must not be null");
        Objects.requireNonNull(date, "date must not be null");

        final String url = String.format(PRICES_URL, pair, DATE_FORMATTER.format(date));
        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("accept", "application/json");

        final HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        final var result = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonPrimitive("amount")
                .getAsDouble();

        return result;
    }

}
