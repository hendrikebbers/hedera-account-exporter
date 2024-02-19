package de.devlodge.hedera.account.export.models;

import java.time.Instant;
import java.util.UUID;

public record Transaction(UUID id, String hederaTransactionId, Instant timestamp, long hbarAmount,
                          double eurAmount, boolean isStakingReward, long hbarBalanceAfterTransaction, double eurBalanceAfterTransaction){}
