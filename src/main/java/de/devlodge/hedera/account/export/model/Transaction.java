package de.devlodge.hedera.account.export.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(Network network, UUID id, String hederaTransactionId, Instant timestamp, BigDecimal hbarAmount,
                          BigDecimal eurAmount, boolean isStakingReward, BigDecimal hbarBalanceAfterTransaction, BigDecimal eurBalanceAfterTransaction){}
