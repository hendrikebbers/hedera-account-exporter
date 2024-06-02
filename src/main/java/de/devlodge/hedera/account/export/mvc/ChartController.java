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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.LongStream;
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
        ChartType chartType = ChartType.fromString(type);
        model.addAttribute("type", chartType.getValue());
        model.addAttribute("headline", chartType.getHeader());
        addChartToModel(model, calculateValues(chartType));
        if(chartType == ChartType.EXCHANGE_RATE || chartType == ChartType.EUR || chartType == ChartType.STACKING_EUR) {
            return "chart-eur";
        } else {
            return "chart-hbar";
        }
    }

    private List<ChartValue> calculateValues(ChartType chartType) {
        final List<Transaction> transactions = transactionService.getTransactions();
        final List<ChartValue> values = new ArrayList<>();
        if(chartType == ChartType.HBAR) {
            transactions.stream()
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .map(t -> {
                        final String xValue = formatTimestamp(t.timestamp());
                        final String yValue = t.balanceAfterTransaction().toString();
                        return new ChartValue(xValue, yValue);
                    })
                    .forEach(values::add);
            final Instant now = Instant.now();
            final String xValue = formatTimestamp(now);
            final String yValue = transactions.get(transactions.size() - 1).balanceAfterTransaction().toString();
            values.add(new ChartValue(xValue, yValue));
        } else if(chartType == ChartType.EUR) {
            transactions.stream()
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .map(t -> {
                        final BigDecimal exchangeRate = getExchangeRate(t);
                        final String xValue = formatTimestamp(t.timestamp());
                        final String yValue = t.balanceAfterTransaction().multiply(exchangeRate).toString();
                        return new ChartValue(xValue, yValue);
                    })
                    .forEach(values::add);
            final Instant now = Instant.now();
            final BigDecimal exchangeRate = getExchangeRate(now);
            final String xValue = formatTimestamp(now);
            final String yValue = transactions.get(transactions.size() - 1).balanceAfterTransaction().multiply(exchangeRate).toString();
            values.add(new ChartValue(xValue, yValue));
        } else if(chartType == ChartType.STACKING_HBAR) {
            final AtomicReference<BigDecimal> cumulativeStakingReward = new AtomicReference<>(new BigDecimal(0));
            transactions.stream()
                    .filter(t -> t.isStakingReward())
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .forEach(t -> {
                        final String xValue = formatTimestamp(t.timestamp());
                        cumulativeStakingReward.set(cumulativeStakingReward.get().add(t.amount()));
                        final String yValue = cumulativeStakingReward.get().doubleValue() + "";
                        values.add(new ChartValue(xValue, yValue));
                    });
        } else if(chartType == ChartType.STACKING_EUR) {
            final AtomicReference<BigDecimal> cumulativeStakingReward = new AtomicReference<>(new BigDecimal(0));
            transactions.stream()
                    .filter(t -> t.isStakingReward())
                    .sorted(Comparator.comparing(Transaction::timestamp))
                    .forEach(t -> {
                        final BigDecimal exchangeRate = getExchangeRate(t);
                        final String xValue = formatTimestamp(t.timestamp());
                        cumulativeStakingReward.set(cumulativeStakingReward.get().add(t.amount()));
                        final String yValue = cumulativeStakingReward.get().multiply(exchangeRate).doubleValue() + "";
                        values.add(new ChartValue(xValue, yValue));
                    });
        } else if(chartType == ChartType.EXCHANGE_RATE) {
            final Instant firstDay = transactions.stream().map(Transaction::timestamp)
                    .sorted(Comparator.naturalOrder()).findFirst()
                    .orElse(Instant.now()).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            final Instant lastDay = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS);

            long days = firstDay.until(lastDay, java.time.temporal.ChronoUnit.DAYS);
            LongStream.range(0, days).forEach(d -> {
                final Instant timestamp = firstDay.plus(d, java.time.temporal.ChronoUnit.DAYS);
                final BigDecimal exchangeRate = getExchangeRate(timestamp);
                final String xValue = formatTimestamp(timestamp);
                final String yValue = exchangeRate.doubleValue() + "";
                values.add(new ChartValue(xValue, yValue));
            });
        }
        return values;
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
