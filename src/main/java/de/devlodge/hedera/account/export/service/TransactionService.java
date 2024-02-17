package de.devlodge.hedera.account.export.service;

import de.devlodge.hedera.account.export.models.Transaction;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
}
