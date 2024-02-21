package de.devlodge.hedera.account.export.service;

import de.devlodge.hedera.account.export.model.Transaction;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
        sort();
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addAllTransactions(Collection<Transaction> entities) {
        transactions.addAll(entities);
        sort();
    }

    private void sort() {
        transactions.sort(Comparator.comparing(Transaction::timestamp));
    }

    public Optional<Transaction> getById(String id) {
        return getById(UUID.fromString(id));
    }
    public Optional<Transaction> getById(UUID id) {
        return transactions.stream().filter(t -> t.id().equals(id)).findFirst();
    }
}
