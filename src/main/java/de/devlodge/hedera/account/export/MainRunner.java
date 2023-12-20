package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.CoinCarpClient;
import de.devlodge.hedera.account.export.clients.HederaClient;
import de.devlodge.hedera.account.export.entities.TransactionEntity;
import de.devlodge.hedera.account.export.repositories.TransactionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class MainRunner implements ApplicationRunner {
    private final TransactionRepository transactionRepository;

    @Autowired
    public MainRunner(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        final String accountId = "0.0.4325052";
        final var results = new HederaClient().request(accountId);

        final var factory = new BalanceTransactionFactory(accountId);
        final var balanceTransactions = factory.create(results);

        final var coinCarpClient = new CoinCarpClient();
        final var transactionEntityFactory = new TransactionEntityFactory(coinCarpClient);

        List<TransactionEntity> entities = transactionEntityFactory.create(balanceTransactions);
        entities.forEach(System.out::println);
        transactionRepository.saveAll(entities);

    }
}
