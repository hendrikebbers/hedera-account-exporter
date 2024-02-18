package de.devlodge.hedera.account.export.clients;

import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.math.BigDecimal;
import java.time.Instant;

public interface ExchangeClient {

    BigDecimal getExchangeRate(final ExchangePair pair, final Instant date) throws Exception;
}
