package de.devlodge.hedera.account.export.factories;

import de.devlodge.hedera.account.export.models.Account;
import de.devlodge.hedera.account.export.clients.ExchangeClient;
import de.devlodge.hedera.account.export.models.BalanceTransaction;
import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.NoteService;
import de.devlodge.hedera.account.export.utils.Currency;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionFactory {
    private final Account account;
    private final ExchangeClient exchangeClient;

    private final NoteService noteService;

    @Autowired
    public TransactionFactory(final ExchangeClient exchangeClient, NoteService noteService) {
        account = new Account();

        this.noteService = Objects.requireNonNull(noteService);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    public List<Transaction> create(final List<BalanceTransaction> balanceTransaction) {
        return balanceTransaction
                .stream()
                .sorted(Comparator.comparing(BalanceTransaction::timestamp))
                .map(transaction -> {
                    try {
                        final String hashOfTransaction = transaction.hederaTransactionId();
                        account.executeTransaction(transaction);

                        final var exchangeRateEur = exchangeClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                                transaction.timestamp());

                        double eurAmount = (((double) transaction.hbarAmount() / 100_000_000))
                                * exchangeRateEur.doubleValue();
                        double eurBalance = (((double) account.getHbarBalance() / 100_000_000))
                                * exchangeRateEur.doubleValue();

                        return new Transaction(UUID.randomUUID(), transaction.hederaTransactionId(), transaction.timestamp(),
                                transaction.hbarAmount(), eurAmount, transaction.isStakingReward(), noteService.getNote(hashOfTransaction).orElse(""),
                                account.getHbarBalance(), eurBalance);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
}
