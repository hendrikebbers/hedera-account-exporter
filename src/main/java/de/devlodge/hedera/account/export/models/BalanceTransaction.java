package de.devlodge.hedera.account.export.models;

import java.time.Instant;

public record BalanceTransaction(String hederaTransactionId, Instant timestamp, long hbarAmount,
                                 boolean isStakingReward) {
}
