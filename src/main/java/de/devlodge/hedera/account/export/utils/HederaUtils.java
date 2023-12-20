package de.devlodge.hedera.account.export.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public final class HederaUtils {
    private static final BigDecimal DIVISOR = BigDecimal.valueOf(100_000_000L);

    public static BigDecimal getHumanReadableAmount(final long amount) {
        return BigDecimal.valueOf(amount).divide(DIVISOR, RoundingMode.HALF_UP);
    }

    public static Instant getInstantFromTimestamp(final String timestamp) {
        final var timestampSplits = timestamp.split("\\.");
        final long seconds = Long.parseLong(timestampSplits[0]);
        final long nanos = Long.parseLong(timestampSplits[1]);
        return Instant.ofEpochSecond(seconds, nanos);
    }

}
