package de.devlodge.hedera.account.export.exchange;

import java.math.BigDecimal;
import java.time.Instant;

public interface ExchangeClient {

    BigDecimal getExchangeRate(final ExchangePair pair, final Instant date) throws Exception;
}
