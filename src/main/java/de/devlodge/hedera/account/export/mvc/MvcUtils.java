package de.devlodge.hedera.account.export.mvc;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MvcUtils {


    public static String getHBarFormatted(BigDecimal hbarAmount) {
        Objects.requireNonNull(hbarAmount, "hbarAmount must not be null");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(true);
        return df.format(hbarAmount) + " ℏ";
    }

    public static String getEurFormatted(BigDecimal eurAmount) {
        Objects.requireNonNull(eurAmount, "eurAmount must not be null");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(true);
        return df.format(eurAmount) + " €";
    }

    public static String formatTimestamp(Instant timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());
        return formatter.format(timestamp);
    }

    public static String formatTransactionLink(String hederaTransactionId) {
        return "https://hashscan.io/mainnet/transaction/" + hederaTransactionId;
    }
}
