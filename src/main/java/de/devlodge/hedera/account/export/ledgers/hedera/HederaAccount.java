package de.devlodge.hedera.account.export.ledgers.hedera;

import java.math.BigDecimal;
import java.util.Objects;

public class HederaAccount {

    private final String accountId;

    private BigDecimal hbarBalance;

    public HederaAccount(final String accountId) {
        this.accountId = Objects.requireNonNull(accountId);
        this.hbarBalance = new BigDecimal(0);
    }

    public BigDecimal addHbarBalance(final BigDecimal amount) {
        Objects.requireNonNull(amount);
        hbarBalance = hbarBalance.add(amount);
        return hbarBalance;
    }

    public BigDecimal getHbarBalance() {
        return hbarBalance;
    }

    public String getAccountId() {
        return accountId;
    }
}
