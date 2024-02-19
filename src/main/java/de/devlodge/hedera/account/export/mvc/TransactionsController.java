package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.NoteService;
import de.devlodge.hedera.account.export.service.TransactionService;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TransactionsController {

    private final TransactionService transactionService;

    private final NoteService noteService;

    @Autowired
    public TransactionsController(final TransactionService transactionService, final NoteService noteService) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.noteService = Objects.requireNonNull(noteService);
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public String showTransactions(final Model model) {
        Objects.requireNonNull(model);
        final List<TransactionModel> transactions = new ArrayList<>();
        final AtomicReference<Double> cumulativeCost = new AtomicReference<Double>(new Double(0));
        transactionService.getTransactions().forEach(t -> {
            TransactionModel transactionModel = convert(cumulativeCost.get(), t);
            cumulativeCost.set(cumulativeCost.get() + t.eurAmount());
            transactions.add(transactionModel);
        });
        model.addAttribute("transactions", transactions);
        return "transactions";
    }

    @RequestMapping(value = "/transaction/{id}", method = RequestMethod.GET)
    public String getById(final Model model, @PathVariable String id) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(id);
        final Optional<Transaction> transaction = transactionService.getById(id);
        if(transaction.isEmpty()) {
            throw new RuntimeException("Transaction with id " + id + " not found");
        } else {
            final TransactionModel transactionModel = transaction.map(t -> convert(0, t)).get();
            model.addAttribute("transaction", transactionModel);
            return "transaction";
        }
    }

    @RequestMapping(value = "/transaction/{id}", method = RequestMethod.POST)
    public String saveNote(final Model model, @ModelAttribute("transaction") TransactionModel transactionModel) {
        Objects.requireNonNull(transactionModel);
        final Transaction transaction = transactionService.getById(transactionModel.id()).orElseThrow();
        noteService.addNote(transaction, transactionModel.note());
        return showTransactions(model);
    }

    private TransactionModel convert(final double cumulativeCostBaseInEur, final Transaction transaction) {
        Objects.requireNonNull(transaction);
        final String note = noteService.getNote(transaction).orElseGet(() -> {
            if (transaction.isStakingReward()) {
                return "Staking Reward";
            } else {
                return "";
            }
        });
        return new TransactionModel(
                transaction.id().toString(),
                transaction.hederaTransactionId(),
                formatTransactionLink(transaction.hederaTransactionId()),
                formatTimestamp(transaction.timestamp()),
                getHBarFormatted(transaction.hbarAmount()),
                getEurFormatted(transaction.eurAmount()),
                getEurFormatted(cumulativeCostBaseInEur + transaction.eurAmount()),
                note,
                getHBarFormatted(transaction.hbarBalanceAfterTransaction()),
                getEurFormatted(transaction.eurBalanceAfterTransaction())
        );
    }

    private static String getHBarFormatted(long hbarAmount) {
        BigDecimal bigDecimal = new BigDecimal(hbarAmount).divide(BigDecimal.valueOf(100_000_000));
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(true);
        return df.format(bigDecimal) + " ℏ";
    }

    private static String getEurFormatted(double eurAmount) {
        BigDecimal bigDecimal = new BigDecimal(eurAmount);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(true);
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

    public record TransactionModel(String id, String hederaTransactionId, String hederaTransactionLink, String timestamp, String hbarAmount, String eurAmount, String cumulativeCostInEur, String note, String hbarBalanceAfterTransaction, String eurBalanceAfterTransaction){}
}
