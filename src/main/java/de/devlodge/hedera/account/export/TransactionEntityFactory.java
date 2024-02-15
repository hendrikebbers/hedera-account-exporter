package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.CoinBaseClient;
import de.devlodge.hedera.account.export.entities.TransactionEntity;
import de.devlodge.hedera.account.export.models.BalanceTransaction;
import de.devlodge.hedera.account.export.utils.Currency;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.util.Comparator;
import java.util.List;

public class TransactionEntityFactory {
    private final Account account;
    private final CoinBaseClient coinBaseClient;

    public TransactionEntityFactory(final CoinBaseClient coinBaseClient) {
        account = new Account();
        this.coinBaseClient = coinBaseClient;
    }

    public List<TransactionEntity> create(final List<BalanceTransaction> balanceTransaction) {
        return balanceTransaction
                .stream()
                .sorted(Comparator.comparing(BalanceTransaction::timestamp))
                .map(transaction -> {
                    final var res = new TransactionEntity();
                    try {
                        account.executeTransaction(transaction);

                        final var exchangeRateEur = coinBaseClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                                transaction.timestamp());

                        double eurAmount = (((double) transaction.hbarAmount() / 100_000_000))
                                * exchangeRateEur;
                        double eurBalance = (((double) account.getHbarBalance() / 100_000_000))
                                * exchangeRateEur;

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
