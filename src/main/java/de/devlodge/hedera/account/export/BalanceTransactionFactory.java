package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.models.BalanceTransaction;
import de.devlodge.hedera.account.export.models.HederaTransaction;
import de.devlodge.hedera.account.export.models.HederaTransfer;
import de.devlodge.hedera.account.export.utils.HederaUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BalanceTransactionFactory {

    private final String accountId;

    public BalanceTransactionFactory(final String accountId) {
        this.accountId = accountId;
    }

    public List<BalanceTransaction> create(final List<HederaTransaction> hederaTransaction) {
        final List<BalanceTransaction> result = new ArrayList<>();

        hederaTransaction.forEach(t -> result.addAll(create(t)));

        return Collections.unmodifiableList(result);
    }

    public List<BalanceTransaction> create(final HederaTransaction hederaTransaction) {
        final List<BalanceTransaction> result = new ArrayList<>();

        result.addAll(convertAll(hederaTransaction, false));
        result.addAll(convertAll(hederaTransaction, true));

        return Collections.unmodifiableList(result);
    }

    private List<BalanceTransaction> convertAll(final HederaTransaction hederaTransaction,
            boolean isStakingReward) {

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
