package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.ExchangeClient;
import de.devlodge.hedera.account.export.factories.HederaTransactionFactory;
import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
import de.devlodge.hedera.account.export.utils.Currency;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MainRunner implements ApplicationRunner {

    private final TransactionService transactionService;

    private final HederaTransactionFactory hederaTransactionFactory;

    private final ExchangeClient exchangeClient;

    private final String accountId;

    @Autowired
    public MainRunner(final TransactionService transactionService,HederaTransactionFactory hederaBalanceTransactionFactory,
            ExchangeClient exchangeClient, @Value("${hedera.export.account}") final String accountId) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.hederaTransactionFactory = Objects.requireNonNull(hederaBalanceTransactionFactory);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
        this.accountId = Objects.requireNonNull(accountId);
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        //1. Get the transactions from Hedera
        List<Transaction> transactions = hederaTransactionFactory.getAllTransactionsForAccount(accountId);

        //3. Save the transactions
        transactionService.addAllTransactions(transactions);

        System.out.println("Transactions saved");

        if(!transactions.isEmpty()) {
            final double hbarAmount = transactions.get(transactions.size() - 1).hbarBalanceAfterTransaction() / 100_000_000;
            final ExchangePair exchangePair = new ExchangePair(Currency.HBAR, Currency.EUR);
            final BigDecimal rate = exchangeClient.getExchangeRate(exchangePair, Instant.now());
            final double eurAmount = hbarAmount  * rate.doubleValue();
            System.out.println("The current value of your HBAR is: " + String.format( "%.2f", eurAmount)  + " €");
        }

        if(!transactions.isEmpty()) {
            final double stackingHbarAmount = (double) transactions.stream()
                    .filter(transaction -> transaction.isStakingReward())
                    .mapToLong(Transaction::hbarAmount)
                    .sum() / 100_000_000;
            System.out.println("Sum of stacked HBAR is: " + String.format( "%.2f", stackingHbarAmount)  + " ℏ");


            final ExchangePair exchangePair = new ExchangePair(Currency.HBAR, Currency.EUR);
            final BigDecimal rate = exchangeClient.getExchangeRate(exchangePair, Instant.now());
            final double eurAmount = stackingHbarAmount  * rate.doubleValue();
            System.out.println("The current value of your stacked HBAR is: " + String.format( "%.2f", eurAmount)  + " €");
        }
    }
}
