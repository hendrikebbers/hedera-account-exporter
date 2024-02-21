package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.ledgers.hedera.HederaClient;
import de.devlodge.hedera.account.export.model.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
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

    private final HederaClient hederaTransactionFactory;

    private final ExchangeClient exchangeClient;

    private final String accountId;

    @Autowired
    public MainRunner(final TransactionService transactionService, HederaClient hederaBalanceTransactionFactory,
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
    }
}
