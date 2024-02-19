package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.ExchangeClient;
import de.devlodge.hedera.account.export.factories.HederaBalanceTransactionFactory;
import de.devlodge.hedera.account.export.factories.TransactionFactory;
import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
import de.devlodge.hedera.account.export.utils.Currency;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MainRunner implements ApplicationRunner {

    private final TransactionService transactionService;

    private final TransactionFactory transactionFactory;

    private final HederaBalanceTransactionFactory hederaBalanceTransactionFactory;

    private ExchangeClient exchangeClient;

    @Autowired
    public MainRunner(final TransactionService transactionService,
            TransactionFactory transactionFactory, HederaBalanceTransactionFactory hederaBalanceTransactionFactory,
            ExchangeClient exchangeClient) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.transactionFactory = Objects.requireNonNull(transactionFactory);
        this.hederaBalanceTransactionFactory = Objects.requireNonNull(hederaBalanceTransactionFactory);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        //1. Get the transactions from Hedera
        final var hederaBalanceTransactions = hederaBalanceTransactionFactory.create();

        //2. Convert the transactions to the format we want -> TODO: steps 1 and 2 should be merged!
        List<Transaction> transactions = transactionFactory.create(hederaBalanceTransactions);

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
    }
}
