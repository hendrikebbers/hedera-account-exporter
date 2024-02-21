package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.NoteService;
import de.devlodge.hedera.account.export.service.TransactionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;

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
    public String showTransactions(final Model model, @RequestParam(value = "print", required = false, defaultValue = "false") boolean print) {
        Objects.requireNonNull(model);
        final List<TransactionModel> transactions = new ArrayList<>();
        final AtomicReference<Double> cumulativeCost = new AtomicReference<Double>(new Double(0));
        transactionService.getTransactions().forEach(t -> {
            TransactionModel transactionModel = convert(cumulativeCost.get(), t);
            cumulativeCost.set(cumulativeCost.get() + t.eurAmount());
            transactions.add(transactionModel);
        });
        model.addAttribute("print", print);
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
        return "redirect:/transactions";
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
        final Map<String, Double> fifo = calculateFifo(transactionService.getTransactions());
        return new TransactionModel(
                transaction.id().toString(),
                transaction.hederaTransactionId(),
                MvcUtils.formatTransactionLink(transaction.hederaTransactionId()),
                MvcUtils.formatTimestamp(transaction.timestamp()),
                MvcUtils.getHBarFormatted(transaction.hbarAmount()),
                MvcUtils.getEurFormatted(transaction.eurAmount()),
                MvcUtils.getEurFormatted(cumulativeCostBaseInEur + transaction.eurAmount()),
                note,
                MvcUtils.getHBarFormatted(transaction.hbarBalanceAfterTransaction()),
                MvcUtils.getEurFormatted(transaction.eurBalanceAfterTransaction()),
                MvcUtils.getEurFormatted(fifo.get(transaction.id().toString()))
        );
    }

    private Map<String, Double> calculateFifo(List<Transaction> transactions) {
            final Map<String, Double> fifoMap = new HashMap<>();
            transactions.forEach(t -> {
                if (t.eurAmount() > 0) {
                    fifoMap.put(t.id().toString(), t.eurAmount());
                } else {
                    fifoMap.put(t.id().toString(), 0.0);
                }
            });

            for(int i = 0; i < transactions.size(); i++) {
                final Transaction transaction = transactions.get(i);
                final double amount = transaction.eurAmount();
                if(amount < 0) {
                    double remainingAmount = -amount;
                    for(int j = 0; j < i; j++) {
                        final Transaction transactionForFifo = transactions.get(j);
                        final double availableFifo = fifoMap.get(transactionForFifo.id().toString());
                        if(availableFifo > 0) {
                            final double fifo = Math.min(availableFifo, remainingAmount);
                            fifoMap.put(transactionForFifo.id().toString(), availableFifo - fifo);
                            remainingAmount =remainingAmount - fifo;
                            if(remainingAmount == 0) {
                                break;
                            }
                        }
                    }
                }
            }
            return Collections.unmodifiableMap(fifoMap);
    }

    public record TransactionModel(String id, String hederaTransactionId, String hederaTransactionLink, String timestamp, String hbarAmount, String eurAmount, String cumulativeCostInEur, String note, String hbarBalanceAfterTransaction, String eurBalanceAfterTransaction, String fifoInEur){}

}
