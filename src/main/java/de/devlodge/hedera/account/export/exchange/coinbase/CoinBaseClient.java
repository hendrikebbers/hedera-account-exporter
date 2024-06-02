package de.devlodge.hedera.account.export.exchange.coinbase;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.model.Currency;
import de.devlodge.hedera.account.export.service.FileService;
import de.devlodge.hedera.account.export.utils.HttpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinBaseClient implements ExchangeClient {

    private final Logger logger = LoggerFactory.getLogger(CoinBaseClient.class);

    private static final String BASE_URL = "https://api.coinbase.com/";
    private static final String PRICES_URL = BASE_URL + "v2/prices/%s/spot?date=%s";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String TIMEZONE_DE = "Europe/Berlin";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD).withZone(ZoneId.of(
            TIMEZONE_DE));

    private final HttpClient client;

    private final List<Exchange> exchanges = new CopyOnWriteArrayList<>();

    private final Path path;


    public CoinBaseClient(final FileService fileService) {
        client = HttpClient.newHttpClient();
        this.path = fileService.getPathForFile("exchanges.txt");
        load();
    }

    private void load() {
        if(Files.exists(path)) {
            try (final InputStream inputStream = Files.newInputStream(path, java.nio.file.StandardOpenOption.CREATE, StandardOpenOption.READ)) {
                final String content = new String(inputStream.readAllBytes());
                final String[] lines = content.split("\n");
                for (String line : lines) {
                    final String[] parts = line.split(",");
                    final LocalDate date = LocalDate.parse(parts[0]);
                    final ExchangePair pair = new ExchangePair(Currency.valueOf(parts[1]), Currency.valueOf(parts[2]));
                    final BigDecimal rate = new BigDecimal(parts[3]);
                    exchanges.add(new Exchange(date, pair, rate));
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not load exchanges", e);
            } catch (Exception e) {
                throw new RuntimeException("Could not load notes", e);
            }
        }
    }

    private void save() {
        try {
            final StringBuilder builder = new StringBuilder();
            for (Exchange exchange : exchanges) {
                builder.append(exchange.date())
                        .append(",")
                        .append(exchange.pair().from())
                        .append(",")
                        .append(exchange.pair().to())
                        .append(",")
                        .append(exchange.rate())
                        .append("\n");
            }
            Files.writeString(path, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Could not save exchanges", e);
        }
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
        try {
            save();
        } catch (Exception e) {
            logger.error("Could not store exchange in file", e);
        }
        return exchange.rate();
    }

    private record Exchange(LocalDate date, ExchangePair pair, BigDecimal rate) {
    }

}
