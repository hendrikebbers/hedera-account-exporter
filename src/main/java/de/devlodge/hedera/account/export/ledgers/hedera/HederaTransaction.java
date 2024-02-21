package de.devlodge.hedera.account.export.ledgers.hedera;

import java.util.List;

public record HederaTransaction(
        String transaction_id,
        String timestamp,
        List<HederaTransfer> hederaTransfers,
        List<HederaTransfer> stakingRewardHederaTransfers
) {
}
