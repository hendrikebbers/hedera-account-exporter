package de.devlodge.hedera.account.export.ledgers;

import de.devlodge.hedera.account.export.model.Transaction;
import java.util.List;

public interface LedgerClient {

    List<Transaction> getAllTransactionsForAccount(final String accountId) throws Exception;
}
