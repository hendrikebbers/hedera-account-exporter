package de.devlodge.hedera.account.export.factories;

import de.devlodge.hedera.account.export.clients.ExchangeClient;
import de.devlodge.hedera.account.export.clients.HederaClient;
import de.devlodge.hedera.account.export.models.Account;
import de.devlodge.hedera.account.export.models.BalanceTransaction;
import de.devlodge.hedera.account.export.models.HederaTransaction;
import de.devlodge.hedera.account.export.models.HederaTransfer;
import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.utils.Currency;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import de.devlodge.hedera.account.export.utils.HederaUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HederaTransactionFactory {

    private final ExchangeClient exchangeClient;

    @Autowired
    public HederaTransactionFactory(ExchangeClient exchangeClient) {
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    public List<Transaction> getAllTransactionsForAccount(final String accountId) throws Exception {
        List<BalanceTransaction> balanceTransactions = create(accountId);
        return create(balanceTransactions, accountId);
    }

    private List<Transaction> create(final List<BalanceTransaction> balanceTransaction, final String accountId) {
        final Account account = new Account();
        return balanceTransaction
                .stream()
                .sorted(Comparator.comparing(BalanceTransaction::timestamp))
                .map(transaction -> {
                    try {
                        final String hashOfTransaction = transaction.hederaTransactionId();
                        account.executeTransaction(transaction);

                        final var exchangeRateEur = exchangeClient.getExchangeRate(new ExchangePair(Currency.HBAR, Currency.EUR),
                                transaction.timestamp());

                        double eurAmount = (((double) transaction.hbarAmount() / 100_000_000))
                                * exchangeRateEur.doubleValue();
                        double eurBalance = (((double) account.getHbarBalance() / 100_000_000))
                                * exchangeRateEur.doubleValue();

                        return new Transaction(UUID.randomUUID(), transaction.hederaTransactionId(), transaction.timestamp(),
                                transaction.hbarAmount(), eurAmount, transaction.isStakingReward(),
                                account.getHbarBalance(), eurBalance);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    private List<BalanceTransaction> create(final String accountId) throws Exception {
        final var hederaTransaction = new HederaClient().request(accountId);
        final List<BalanceTransaction> result = new ArrayList<>();
        hederaTransaction.forEach(t -> result.addAll(create(t, accountId)));
        return Collections.unmodifiableList(result);
    }

    private List<BalanceTransaction> create(final HederaTransaction hederaTransaction, final String accountId) {
        final List<BalanceTransaction> result = new ArrayList<>();

        result.addAll(convertAll(hederaTransaction, false, accountId));
        result.addAll(convertAll(hederaTransaction, true, accountId));

        return Collections.unmodifiableList(result);
    }

    private List<BalanceTransaction> convertAll(final HederaTransaction hederaTransaction,
            boolean isStakingReward, final String accountId) {

        final var hederaTransfers = isStakingReward
                ? hederaTransaction.stakingRewardHederaTransfers()
                : hederaTransaction.hederaTransfers();

        return hederaTransfers
                .stream()
                .filter(t -> t.account().equals(accountId))
                .map(t -> convert(hederaTransaction, t, isStakingReward))
                .toList();
    }

    private static BalanceTransaction convert(
            final HederaTransaction hederaTransaction,
            final HederaTransfer hederaTransfer,
            boolean isStakingReward) {
        return new BalanceTransaction(
                hederaTransaction.transaction_id(),
                HederaUtils.getInstantFromTimestamp(hederaTransaction.timestamp()),
                hederaTransfer.amount(),
                isStakingReward
        );
    }
}
