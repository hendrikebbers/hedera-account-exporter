package de.devlodge.hedera.account.export.mvc;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MvcUtils {

    public static String getHBarFormatted(long hbarAmount) {
        return getHBarFormatted((double) hbarAmount);
    }

    public static String getHBarFormatted(double hbarAmount) {
        BigDecimal bigDecimal = new BigDecimal(hbarAmount).divide(BigDecimal.valueOf(100_000_000));
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(true);
        return df.format(bigDecimal) + " ℏ";
    }

    public static String getEurFormatted(double eurAmount) {
        BigDecimal bigDecimal = new BigDecimal(eurAmount);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setGroupingUsed(true);
        return df.format(bigDecimal) + " €";
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
