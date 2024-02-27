package de.devlodge.hedera.account.export.mvc;

import com.google.gson.JsonArray;
import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.model.Currency;
import de.devlodge.hedera.account.export.model.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChartController {

    private final TransactionService transactionService;

    private final ExchangeClient exchangeClient;

    @Autowired
    public ChartController(final TransactionService transactionService, ExchangeClient exchangeClient) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    private BigDecimal getExchangeRate(final Transaction transaction) {
        try {
            return exchangeClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                    transaction.timestamp());
        } catch (Exception e) {
            throw new RuntimeException("Can not get exchange rate", e);
        }
    }

    @RequestMapping(value = "/chart", method = RequestMethod.GET)
    public String hello(final Model model, @RequestParam(value = "type", required = false, defaultValue = "eur") String type) {
        Objects.requireNonNull(model);
        model.addAttribute("type", type);

        final List<Value> values = new ArrayList<>();
        transactionService.getTransactions().forEach(t -> {
            final String timestamp = formatTimestamp(t.timestamp());
            final double hbarAmount = t.hbarBalanceAfterTransaction().doubleValue();
            final BigDecimal exchangeRate = getExchangeRate(t);
            final double eurAmount = t.hbarBalanceAfterTransaction().multiply(exchangeRate).doubleValue();
            values.add(new Value(timestamp, hbarAmount, eurAmount));
        });
        values.sort(Comparator.comparing(v -> v.timestamp));
        JsonArray xValues = new JsonArray();
        values.forEach(v -> xValues.add(v.timestamp));
        model.addAttribute("xValues", xValues.toString().replaceAll("\"", ""));

        final JsonArray hbarValues = new JsonArray();
        values.forEach(v -> hbarValues.add(v.hbarAmount));
        model.addAttribute("hbarValues", hbarValues.toString());

        final JsonArray eurValues = new JsonArray();
        values.forEach(v -> eurValues.add(v.eurAmount));
        model.addAttribute("eurValues", eurValues.toString());
        return "chart";
    }

    private static String formatTimestamp(Instant timestamp) {
        //moment('2016-03-12 13:00:00')
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                .withZone(ZoneId.systemDefault());
        return "'" + formatter.format(timestamp) + "'";
    }

    public record Value(String timestamp, double hbarAmount, double eurAmount){}

}
