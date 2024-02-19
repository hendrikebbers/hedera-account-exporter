package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.CoinBaseClient;
import de.devlodge.hedera.account.export.clients.HederaClient;
import de.devlodge.hedera.account.export.models.Transaction;
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
    private final String accountId;

    private final TransactionService transactionService;

    @Autowired
    public MainRunner(final TransactionService transactionService,
            @Value("${hedera.export.account}") final String accountId) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.accountId = accountId;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {


        final var results = new HederaClient().request(accountId);

        final var factory = new BalanceTransactionFactory(accountId);
        final var balanceTransactions = factory.create(results);

        final var coinBaseClient = new CoinBaseClient();
        final var transactionEntityFactory = new TransactionEntityFactory(coinBaseClient);

        List<Transaction> entities = transactionEntityFactory.create(balanceTransactions);
        transactionService.addAllTransactions(entities);
        System.out.println("Transactions saved");
    }
}
