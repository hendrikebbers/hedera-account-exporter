package de.devlodge.hedera.account.export.mvc;

import com.google.gson.JsonArray;
import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.model.Currency;
import de.devlodge.hedera.account.export.model.Network;
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
import java.util.UUID;
import java.util.function.Function;
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

    @RequestMapping(value = "/chart", method = RequestMethod.GET)
    public String chart(final Model model, @RequestParam(value = "type", required = false, defaultValue = "eur") String type) {
        Objects.requireNonNull(model);
        model.addAttribute("type", type);
        final List<Transaction> transactions = transactionService.getTransactions();

        if(type.equals("hbar")) {
            final List<ChartValue> converted = transactions.stream()
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .map(t -> {
                        final String xValue = formatTimestamp(t.timestamp());
                        final String yValue = t.hbarBalanceAfterTransaction().toString();
                        return new ChartValue(xValue, yValue);
                    })
                    .toList();
            final List<ChartValue> values = new ArrayList<>(converted);
            final Instant now = Instant.now();
            final String xValue = formatTimestamp(now);
            final String yValue = transactions.get(transactions.size() - 1).hbarBalanceAfterTransaction().toString();
            values.add(new ChartValue(xValue, yValue));
            addChartToModel(model, values);
        } else if(type.equals("eur")) {
            final List<ChartValue> converted = transactions.stream()
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .map(t -> {
                        final BigDecimal exchangeRate = getExchangeRate(t);
                        final String xValue = formatTimestamp(t.timestamp());
                        final String yValue = t.hbarBalanceAfterTransaction().multiply(exchangeRate).toString();
                        return new ChartValue(xValue, yValue);
                    })
                    .toList();
            final List<ChartValue> values = new ArrayList<>(converted);
            final Instant now = Instant.now();
            final BigDecimal exchangeRate = getExchangeRate(now);
            final String xValue = formatTimestamp(now);
            final String yValue = transactions.get(transactions.size() - 1).hbarBalanceAfterTransaction().multiply(exchangeRate).toString();
            values.add(new ChartValue(xValue, yValue));
            addChartToModel(model, values);
        } else if(type.equals("staking")) {
            final List<Transaction> converted = transactions.stream()
                    .filter(t -> t.isStakingReward())
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .toList();
            final List<ChartValue> values = new ArrayList<>();
            for (Transaction transaction : converted) {
                final String xValue = formatTimestamp(transaction.timestamp());
                if(values.isEmpty()) {
                    values.add(new ChartValue(xValue, transaction.amount().toString()));
                } else {
                    final String yValue = (Double.valueOf(values.get(values.size() - 1).yValue) + transaction.amount().doubleValue()) + "";
                    values.add(new ChartValue(xValue, yValue));
                }
            }
            addChartToModel(model, values);
        }
        return "chart";
    }

    private void addChartToModel(final Model model, final List<ChartValue> values) {
        model.addAttribute("xValues", convertToChartValues(values, v -> v.xValue));
        model.addAttribute("yValues", convertToChartValues(values, v -> v.yValue));
    }

    private BigDecimal getExchangeRate(final Transaction transaction) {
        return getExchangeRate(transaction.timestamp());
    }

    private BigDecimal getExchangeRate(final Instant timestamp) {
        try {
            return exchangeClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                    timestamp);
        } catch (Exception e) {
            throw new RuntimeException("Can not get exchange rate", e);
        }
    }

    private <T> String convertToChartValues(List<T> values, Function<T, Object> valueFunction) {
        JsonArray jsonArray = new JsonArray();
        values.forEach(v -> jsonArray.add(valueFunction.apply(v).toString()));
        return jsonArray.toString();
    }

    private static String formatTimestamp(Instant timestamp) {
        //moment('2016-03-12 13:00:00')
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                .withZone(ZoneId.systemDefault());
        return formatter.format(timestamp);
    }

    private record ChartValue(String xValue, String yValue) {}
}
