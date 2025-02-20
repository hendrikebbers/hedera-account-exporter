package de.devlodge.hedera.account.export.session;

import de.devlodge.hedera.account.export.ledgers.hedera.HederaClient;
import de.devlodge.hedera.account.export.model.Transaction;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class SessionStore {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    private String accountId;

    private final HederaClient hederaTransactionFactory;

    @Autowired
    public SessionStore(HederaClient hederaTransactionFactory) {
        this.hederaTransactionFactory = Objects.requireNonNull(hederaTransactionFactory, "hederaTransactionFactory");
    }

    public synchronized Optional<String> accountId() {
        return Optional.ofNullable(accountId);
    }

    public synchronized String getAccountId() {
        return accountId;
    }

    public synchronized void setAccountId(String accountId) throws Exception {
        this.accountId = accountId;
        this.transactions.clear();
        if (accountId != null) {
            try {
                hederaTransactionFactory.getAllTransactionsForAccount(accountId)
                        .forEach(transaction -> transactions.add(transaction));
            } catch (final Exception e) {
                this.accountId = null;
                throw new RuntimeException("Can not get transactions for account", e);
            }
            transactions.sort(Comparator.comparing(Transaction::timestamp));
        }
    }

    public synchronized List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public synchronized Optional<Transaction> getById(String id) {
        return getById(UUID.fromString(id));
    }

    public synchronized Optional<Transaction> getById(UUID id) {
        return transactions.stream().filter(t -> t.id().equals(id)).findFirst();
    }
}
