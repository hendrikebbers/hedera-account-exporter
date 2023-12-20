package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.CoinCarpClient;
import de.devlodge.hedera.account.export.clients.CoinCarpCodes;
import de.devlodge.hedera.account.export.entities.TransactionEntity;
import de.devlodge.hedera.account.export.models.BalanceTransaction;
import java.util.Comparator;
import java.util.List;

public class TransactionEntityFactory {
    private final Account account;
    private final CoinCarpClient coinCarpClient;

    public TransactionEntityFactory(final CoinCarpClient coinCarpClient) {
        account = new Account();
        this.coinCarpClient = coinCarpClient;
    }

    public List<TransactionEntity> create(final List<BalanceTransaction> balanceTransaction) {
        return balanceTransaction
                .stream()
                .sorted(Comparator.comparing(BalanceTransaction::timestamp))
                .map(transaction -> {
                    final var res = new TransactionEntity();
                    try {
                        account.executeTransaction(transaction);

                        final var exchangeRateHbar = coinCarpClient.getExchangeRate(CoinCarpCodes.HBAR,
                                transaction.timestamp());
                        final var exchangeRateEur = coinCarpClient.getExchangeRate(CoinCarpCodes.EUR,
                                transaction.timestamp());

                        double eurAmount = (((double) transaction.hbarAmount() / 100_000_000) * exchangeRateHbar)
                                / exchangeRateEur;
                        double eurBalance = (((double) account.getHbarBalance() / 100_000_000) * exchangeRateHbar)
                                / exchangeRateEur;

                        res.setHederaTransactionId(transaction.hederaTransactionId());
                        res.setTimestamp(transaction.timestamp());
                        res.setHbarAmount(transaction.hbarAmount());
                        res.setEurAmount(eurAmount);
                        res.setStakingReward(transaction.isStakingReward());
                        res.setHbarBalanceAfterTransaction(account.getHbarBalance());
                        res.setEurBalanceAfterTransaction(eurBalance);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return res;
                })
                .toList();
    }
}
