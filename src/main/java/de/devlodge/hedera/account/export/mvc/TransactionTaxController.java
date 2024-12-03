package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.model.Currency;
import de.devlodge.hedera.account.export.model.Transaction;
import de.devlodge.hedera.account.export.service.NoteService;
import de.devlodge.hedera.account.export.service.TransactionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TransactionTaxController {

    private final TransactionService transactionService;

    private final NoteService noteService;

    private final ExchangeClient exchangeClient;

    @Autowired
    public TransactionTaxController(final TransactionService transactionService, final NoteService noteService,
            ExchangeClient exchangeClient) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.noteService = Objects.requireNonNull(noteService);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    @RequestMapping(value = "/transactions-tax", method = RequestMethod.GET)
    public String showTransactionsTax(final Model model) {
        Objects.requireNonNull(model);

        final List<TransactionWithFifo> transactionsWithFifo = new ArrayList<>();
        final List<Transaction> transactions = transactionService.getTransactions();
        transactions.forEach(t -> {
            final BigDecimal exchangeRate = getExchangeRate(t);
            final List<TransactionWithFifo> preTransactionsWithFifo = new ArrayList<>(transactionsWithFifo);
            final String note = noteService.getNote(t).orElseGet(() -> {
                if (t.isStakingReward()) {
                    return "Staking Reward";
                } else {
                    return "";
                }
            });
            final TransactionWithFifo transactionWithFifo = new TransactionWithFifo(transactions.indexOf(t), t,
                    exchangeRate,
                    preTransactionsWithFifo, note);
            transactionsWithFifo.add(transactionWithFifo);
        });
        model.addAttribute("transactions", transactionsWithFifo.stream().map(TransactionWithFifo::toTransactionTaxModel)
                .toList());
        return "transactions-tax";
    }

    private class TransactionWithFifo {

        private final int index;

        private final Transaction transaction;

        private final BigDecimal exchangeRate;

        private final BigDecimal cumulativeCostInEur;

        private BigDecimal openFifoInHBAR;

        private final List<FifoUsage> usedFifoFor = new ArrayList<>();

        private final List<FifoUsage> usedFifoBy = new ArrayList<>();

        private final String note;

        public TransactionWithFifo(int index, Transaction transaction, final BigDecimal exchangeRate,
                List<TransactionWithFifo> preTransactionsWithFifo, String note) {
            this.index = index;
            this.transaction = transaction;
            this.exchangeRate = exchangeRate;
            this.note = note;
            final BigDecimal costInEur = transaction.amount().multiply(exchangeRate)
                    .setScale(2, RoundingMode.HALF_UP);
            final BigDecimal prevCumulativeCostInEur = preTransactionsWithFifo.stream()
                    .map(t -> t.transaction.amount().multiply(t.exchangeRate))
                    .map(d -> d.setScale(2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            this.cumulativeCostInEur = prevCumulativeCostInEur.setScale(2, RoundingMode.HALF_UP)
                    .add(costInEur).setScale(2, RoundingMode.HALF_UP);

            if (isPositive(transaction.amount())) {
                openFifoInHBAR = transaction.amount();
            } else {
                openFifoInHBAR = BigDecimal.ZERO;
            }
            if (isNegative(transaction.amount())) {
                BigDecimal remainingAmount = transaction.amount().abs();
                for (TransactionWithFifo preTransactionWithFifo : preTransactionsWithFifo) {
                    if (isZero(remainingAmount)) {
                        break;
                    }
                    if (isPositive(preTransactionWithFifo.openFifoInHBAR)) {
                        final BigDecimal fifo = preTransactionWithFifo.openFifoInHBAR.min(remainingAmount);
                        preTransactionWithFifo.openFifoInHBAR = preTransactionWithFifo.openFifoInHBAR.subtract(fifo);
                        remainingAmount = remainingAmount.subtract(fifo);
                        final BigDecimal fifoInEur = fifo.multiply(preTransactionWithFifo.exchangeRate)
                                .setScale(2, RoundingMode.HALF_UP);
                        usedFifoBy.add(new FifoUsage(preTransactionWithFifo.index, fifo, fifoInEur));
                        preTransactionWithFifo.usedFifoFor.add(new FifoUsage(index, fifo.negate(), fifoInEur.negate()));
                    }
                }
                if (!isZero(remainingAmount)) {
                    throw new RuntimeException("Can not calculate FIFO. Remaining Fifo=" + remainingAmount
                            + " for transaction " + transaction.id() + " with amount " + transaction.amount());
                }
            }
        }


        public TransactionTaxModel toTransactionTaxModel() {
            final String usedFifoByString = usedFifoBy.stream()
                    .map(i -> i.index + " (" + MvcUtils.getHBarFormatted(i.amountInHBAR) + " / "
                            + MvcUtils.getEurFormatted(i.amountInEur) + ")")
                    .reduce((a, b) -> a + System.lineSeparator() + b)
                    .orElseGet(() -> "");
            final String usedFifoForString = usedFifoFor.stream()
                    .map(i -> i.index + " (" + MvcUtils.getHBarFormatted(i.amountInHBAR) + " / "
                            + MvcUtils.getEurFormatted(i.amountInEur) + ")")
                    .reduce((a, b) -> a + System.lineSeparator() + b)
                    .orElseGet(() -> "");
            final String usedFifoString;
            if (usedFifoByString.isEmpty()) {
                usedFifoString = usedFifoForString;
            } else if (usedFifoForString.isEmpty()) {
                usedFifoString = usedFifoByString;
            } else {
                usedFifoString = usedFifoByString + System.lineSeparator() + usedFifoForString;
            }
            return new TransactionTaxModel(index,
                    transaction.id().toString(),
                    transaction.networkId(),
                    MvcUtils.formatTimestamp(transaction.timestamp()),
                    MvcUtils.getHBarFormatted(transaction.amount()),
                    MvcUtils.getEurFormatted(
                            transaction.amount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP)),
                    MvcUtils.getEurFormatted(cumulativeCostInEur),
                    note,
                    MvcUtils.getHBarFormatted(transaction.balanceAfterTransaction()),
                    MvcUtils.getEurFormatted(openFifoInHBAR.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP)),
                    MvcUtils.getEurFormatted(exchangeRate, 6),
                    usedFifoString);
        }
    }

    private static boolean isPositive(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean isNegative(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    private static boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal getExchangeRate(final Transaction transaction) {
        try {
            return exchangeClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                    transaction.timestamp());
        } catch (Exception e) {
            throw new RuntimeException("Can not get exchange rate", e);
        }
    }

    public record TransactionTaxModel(int index,
                                      String id,
                                      String hederaTransactionId,
                                      String timestamp,
                                      String hbarAmount,
                                      String eurAmount,
                                      String cumulativeCostInEur,
                                      String note,
                                      String hbarBalanceAfterTransaction,
                                      String fifoInEur,
                                      String exchangeRate,
                                      String fifoUsage) {
    }

    public record FifoUsage(int index, BigDecimal amountInHBAR, BigDecimal amountInEur) {
    }

}
