package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.factories.HederaBalanceTransactionFactory;
import de.devlodge.hedera.account.export.factories.TransactionFactory;
import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
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

    @Autowired
    public MainRunner(final TransactionService transactionService,
            TransactionFactory transactionFactory, HederaBalanceTransactionFactory hederaBalanceTransactionFactory) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.transactionFactory = Objects.requireNonNull(transactionFactory);
        this.hederaBalanceTransactionFactory = Objects.requireNonNull(hederaBalanceTransactionFactory);
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        //1. Get the transactions from Hedera
        final var hederaBalanceTransactions = hederaBalanceTransactionFactory.create();

        //2. Convert the transactions to the format we want -> TODO: steps 1 and 2 should be merged!
        List<Transaction> entities = transactionFactory.create(hederaBalanceTransactions);

        //3. Save the transactions
        transactionService.addAllTransactions(entities);

        System.out.println("Transactions saved");
    }
}
