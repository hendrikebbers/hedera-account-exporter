package de.devlodge.hedera.account.export.clients;

import com.google.gson.JsonParser;
import de.devlodge.hedera.account.export.models.CoinCarpExchangeRate;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Deprecated(forRemoval = true)
public class CoinCarpClient {

    private final static String BASE_URL = "https://sapi.coincarp.com/api/v1/his/coin/histicker?code=%s&begintime=%s&endtime=%s&lang=en-US";

    private final HttpClient client;

    public CoinCarpClient() {
        client = HttpClient.newHttpClient();
    }

    public double getExchangeRate(final CoinCarpCodes code, final Instant instant)
            throws IOException, InterruptedException {
        final var start = instant.minus(1, ChronoUnit.DAYS);
        final String url = BASE_URL.formatted(code.getCode(), start.getEpochSecond(), instant.getEpochSecond());

        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("accept", "application/json");

        final HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        final var jsonArray = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .get("data")
                .getAsJsonArray()
                .get(0)
                .getAsJsonArray();
        final var result = new CoinCarpExchangeRate(jsonArray.get(0).getAsLong(),
                jsonArray.get(1).getAsDouble(),
                jsonArray.get(2).getAsDouble(),
                jsonArray.get(3).getAsDouble(),
                jsonArray.get(4).getAsDouble()
        );

        return result.closeRate();
    }
}
