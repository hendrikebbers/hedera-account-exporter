package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.model.Currency;
import de.devlodge.hedera.account.export.model.Transaction;
import de.devlodge.hedera.account.export.service.NoteService;
import de.devlodge.hedera.account.export.service.TransactionService;
import java.math.BigDecimal;
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

    private final ExchangeClient exchangeClient;

    @Autowired
    public TransactionsController(final TransactionService transactionService, final NoteService noteService,
            ExchangeClient exchangeClient) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.noteService = Objects.requireNonNull(noteService);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public String showTransactions(final Model model, @RequestParam(value = "print", required = false, defaultValue = "false") boolean print) {
        Objects.requireNonNull(model);
        final List<TransactionModel> transactions = new ArrayList<>();
        final AtomicReference<BigDecimal> cumulativeCost = new AtomicReference<BigDecimal>(new BigDecimal(0));
        transactionService.getTransactions().forEach(t -> {
            final TransactionModel transactionModel = convert(cumulativeCost.get(), t);
            transactions.add(transactionModel);

            final BigDecimal exchangeRate =getExchangeRate(t);
            final BigDecimal eurAmount = t.amount().multiply(exchangeRate);
            final BigDecimal newCumulativeCost = cumulativeCost.get()
                    .add(eurAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            cumulativeCost.set(newCumulativeCost);
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
            final TransactionModel transactionModel = transaction.map(t -> convert(new BigDecimal(0), t)).get();
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

    private TransactionModel convert(final BigDecimal cumulativeCostBaseInEur, final Transaction transaction) {
        Objects.requireNonNull(transaction);
        final String note = noteService.getNote(transaction).orElseGet(() -> {
            if (transaction.isStakingReward()) {
                return "Staking Reward";
            } else {
                return "";
            }
        });
        final Map<String, BigDecimal> fifo = calculateFifo(transactionService.getTransactions());

        final BigDecimal exchangeRate = getExchangeRate(transaction);
        final BigDecimal eurAmount = transaction.amount().multiply(exchangeRate);
        final BigDecimal eurBalanceAfterTransaction = transaction.hbarBalanceAfterTransaction().multiply(exchangeRate);

        final BigDecimal newCumulativeCost = cumulativeCostBaseInEur
                .add(eurAmount)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        return new TransactionModel(
                transaction.id().toString(),
                transaction.networkId(),
                MvcUtils.formatTransactionLink(transaction.networkId()),
                MvcUtils.formatTimestamp(transaction.timestamp()),
                MvcUtils.getHBarFormatted(transaction.amount()),
                MvcUtils.getEurFormatted(eurAmount),
                MvcUtils.getEurFormatted(newCumulativeCost),
                note,
                MvcUtils.getHBarFormatted(transaction.hbarBalanceAfterTransaction()),
                MvcUtils.getEurFormatted(eurBalanceAfterTransaction),
                MvcUtils.getEurFormatted(fifo.get(transaction.id().toString()))
        );
    }

    private BigDecimal getExchangeRate(final Transaction transaction) {
        try {
            return exchangeClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                    transaction.timestamp());
        } catch (Exception e) {
            throw new RuntimeException("Can not get exchange rate", e);
        }
    }

    private Map<String, BigDecimal> calculateFifo(List<Transaction> transactions) {
            final Map<String, BigDecimal> fifoMap = new HashMap<>();
            transactions.forEach(t -> {
                if (t.amount().doubleValue() > 0) {
                    final BigDecimal exchangeRate = getExchangeRate(t);
                    final BigDecimal eurAmount = t.amount().multiply(exchangeRate);
                    fifoMap.put(t.id().toString(), eurAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
                } else {
                    fifoMap.put(t.id().toString(), new BigDecimal(0.0));
                }
            });

            for(int i = 0; i < transactions.size(); i++) {
                final Transaction transaction = transactions.get(i);
                final BigDecimal exchangeRate = getExchangeRate(transaction);
                final BigDecimal eurAmount = transaction.amount().multiply(exchangeRate);
                final BigDecimal amount = eurAmount
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                if(amount.doubleValue() < 0) {
                    BigDecimal remainingAmount = amount.multiply(new BigDecimal(-1))
                            .setScale(2, BigDecimal.ROUND_HALF_UP);
                    for(int j = 0; j < i; j++) {
                        final Transaction transactionForFifo = transactions.get(j);
                        final BigDecimal availableFifo = fifoMap.get(transactionForFifo.id().toString());
                        if(availableFifo.doubleValue() > 0) {
                            final BigDecimal fifo = availableFifo.min(remainingAmount);
                            fifoMap.put(transactionForFifo.id().toString(), availableFifo.subtract(fifo).setScale(2, BigDecimal.ROUND_HALF_UP));
                            remainingAmount =remainingAmount.subtract(fifo);
                            if(remainingAmount.doubleValue() == 0) {
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
