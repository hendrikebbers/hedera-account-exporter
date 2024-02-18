package de.devlodge.hedera.account.export.mvc;

import com.google.gson.JsonArray;
import de.devlodge.hedera.account.export.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ChartController {

    private final TransactionService transactionService;

    @Autowired
    public ChartController(final TransactionService transactionService) {
        this.transactionService = Objects.requireNonNull(transactionService);
    }

    @RequestMapping(value = "/chart", method = RequestMethod.GET)
    public String hello(final Model model) {
        Objects.requireNonNull(model);
        final List<Value> values = new ArrayList<>();
        transactionService.getTransactions().forEach(t -> values.add(new Value(
                formatTimestamp(t.timestamp()),
                getHBarFormatted(t.hbarBalanceAfterTransaction()),
                t.eurBalanceAfterTransaction()
        )));
        values.sort((v1, v2) -> v1.timestamp.compareTo(v2.timestamp));
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

    private static double getHBarFormatted(long hbarAmount) {
        BigDecimal hbar = new BigDecimal(hbarAmount).divide(BigDecimal.valueOf(100_000_000));
        return hbar.doubleValue();
    }

    public record Value(String timestamp, double hbarAmount, double eurAmount){}

}
