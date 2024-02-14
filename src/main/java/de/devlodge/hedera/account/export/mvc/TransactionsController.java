package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TransactionsController {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionsController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public String hello(final Model model) {
        Objects.requireNonNull(model);
        final List<TransactionModel> transactions = new ArrayList<>();
        transactionRepository.findAll().forEach(t -> transactions.add(new TransactionModel(
                t.getHederaTransactionId(),
                formatTransactionLink(t.getHederaTransactionId()),
                formatTimestamp(t.getTimestamp()),
                getHBarFormatted(t.getHbarAmount()),
                getEurFormatted(t.getEurAmount()),
                Optional.ofNullable(t.getNote()).orElse(""),
                getHBarFormatted(t.getHbarBalanceAfterTransaction()),
                getEurFormatted(t.getEurBalanceAfterTransaction())
        )));
        model.addAttribute("transactions", transactions);
        return "transactions";
    }

    private static String getHBarFormatted(long hbarAmount) {
        BigDecimal hbar = new BigDecimal(hbarAmount).divide(BigDecimal.valueOf(100000000));
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(false);
        return df.format(hbar) + " ℏ";
    }

    private static String getEurFormatted(double eurAmount) {
        BigDecimal bigDecimal = new BigDecimal(eurAmount);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(false);
        return df.format(bigDecimal) + " €";
    }

    private static String formatTimestamp(Instant timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());
        return formatter.format(timestamp);
    }

    private static String formatTransactionLink(String hederaTransactionId) {
        return "https://hashscan.io/mainnet/transaction/" + hederaTransactionId;
    }

    public record TransactionModel(String hederaTransactionId, String hederaTransactionLink, String timestamp, String hbarAmount, String eurAmount, String note, String hbarBalanceAfterTransaction, String eurBalanceAfterTransaction){}
}
