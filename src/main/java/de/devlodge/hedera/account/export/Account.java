package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.models.BalanceTransaction;

public class Account {
    private long hbarBalance;

    public Account() {
        this(0);
    }

    public Account(final long hbarBalance) {
        this.hbarBalance = hbarBalance;
    }

    public long getHbarBalance() {
        return hbarBalance;
    }

    public void executeTransaction(final BalanceTransaction transaction) {
        hbarBalance += transaction.hbarAmount();
    }
}
