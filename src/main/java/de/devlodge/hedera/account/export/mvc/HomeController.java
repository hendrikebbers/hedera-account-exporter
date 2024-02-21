package de.devlodge.hedera.account.export.mvc;

import com.google.gson.JsonArray;
import de.devlodge.hedera.account.export.clients.ExchangeClient;
import de.devlodge.hedera.account.export.models.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
import de.devlodge.hedera.account.export.utils.Currency;
import de.devlodge.hedera.account.export.utils.ExchangePair;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final TransactionService transactionService;

    private final ExchangeClient exchangeClient;


    @Autowired
    public HomeController(final TransactionService transactionService, ExchangeClient exchangeClient) {
        this.transactionService = Objects.requireNonNull(transactionService);
        this.exchangeClient = Objects.requireNonNull(exchangeClient);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello(final Model model) throws Exception {
        Objects.requireNonNull(model);
        final List<Transaction> transactions = transactionService.getTransactions();
        final ExchangePair exchangePair = new ExchangePair(Currency.HBAR, Currency.EUR);
        final BigDecimal exchangeRate = exchangeClient.getExchangeRate(exchangePair, Instant.now());
        model.addAttribute("exchangeRate", MvcUtils.getEurFormatted(exchangeRate.doubleValue()));

        if(!transactions.isEmpty()) {
            final double hbarAmount = transactions.get(transactions.size() - 1).hbarBalanceAfterTransaction();
            model.addAttribute("hbarAmount", MvcUtils.getHBarFormatted(hbarAmount));

            final double eurAmount = (hbarAmount / 100_000_000) * exchangeRate.doubleValue();
            model.addAttribute("eurAmount", MvcUtils.getEurFormatted(eurAmount));

            final long stackingHbarAmount = transactions.stream()
                    .filter(transaction -> transaction.isStakingReward())
                    .mapToLong(Transaction::hbarAmount)
                    .sum();
            model.addAttribute("stackedHbar", MvcUtils.getHBarFormatted(stackingHbarAmount));

            final double stackingEurAmount = (stackingHbarAmount / 100_000_000)  * exchangeRate.doubleValue();
            model.addAttribute("stackedEur", MvcUtils.getEurFormatted(stackingEurAmount));
        }
        return "home";
    }
}
