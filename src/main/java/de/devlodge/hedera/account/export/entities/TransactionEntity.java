package de.devlodge.hedera.account.export.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
public class TransactionEntity {
    @Id
    @GeneratedValue
    private UUID id;

    private String hederaTransactionId;
    private Instant timestamp;
    private long hbarAmount;
    private double eurAmount;
    private boolean isStakingReward;
    private String note;
    private long hbarBalanceAfterTransaction;
    private double eurBalanceAfterTransaction;

    @Override
    public String toString() {
        return
                id +
                        "," + hederaTransactionId +
                        "," + timestamp +
                        "," + hbarAmount +
                        "," + eurAmount +
                        "," + isStakingReward +
                        "," + Objects.requireNonNullElse(note, "") +
                        "," + hbarBalanceAfterTransaction +
                        "," + eurBalanceAfterTransaction;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getHederaTransactionId() {
        return hederaTransactionId;
    }

    public void setHederaTransactionId(final String hederaTransactionId) {
        this.hederaTransactionId = hederaTransactionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
    }

    public long getHbarAmount() {
        return hbarAmount;
    }

    public void setHbarAmount(long hbarAmount) {
        this.hbarAmount = hbarAmount;
    }

    public double getEurAmount() {
        return eurAmount;
    }

    public void setEurAmount(double eurAmount) {
        this.eurAmount = eurAmount;
    }

    public boolean isStakingReward() {
        return isStakingReward;
    }

    public void setStakingReward(boolean stakingReward) {
        isStakingReward = stakingReward;
    }

    public String getNote() {
        return note;
    }

    public void setNote(final String note) {
        this.note = note;
    }

    public long getHbarBalanceAfterTransaction() {
        return hbarBalanceAfterTransaction;
    }

    public void setHbarBalanceAfterTransaction(long hbarBalanceAfterTransaction) {
        this.hbarBalanceAfterTransaction = hbarBalanceAfterTransaction;
    }

    public double getEurBalanceAfterTransaction() {
        return eurBalanceAfterTransaction;
    }

    public void setEurBalanceAfterTransaction(double eurBalanceAfterTransaction) {
        this.eurBalanceAfterTransaction = eurBalanceAfterTransaction;
    }
}
