package de.devlodge.hedera.account.export.mvc;

import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.model.Transaction;
import de.devlodge.hedera.account.export.service.TransactionService;
import de.devlodge.hedera.account.export.model.Currency;
import de.devlodge.hedera.account.export.exchange.ExchangePair;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
        model.addAttribute("exchangeRate", MvcUtils.getEurFormatted(exchangeRate));

        if(!transactions.isEmpty()) {
            final BigDecimal hbarAmount = transactions.get(transactions.size() - 1).balanceAfterTransaction();
            model.addAttribute("hbarAmount", MvcUtils.getHBarFormatted(hbarAmount));

            final BigDecimal eurAmount = hbarAmount.multiply(exchangeRate);
            model.addAttribute("eurAmount", MvcUtils.getEurFormatted(eurAmount));

            final BigDecimal stackingHbarAmount = transactions.stream()
                    .filter(transaction -> transaction.isStakingReward())
                    .map(Transaction::amount)
                            .reduce(new BigDecimal(0), (a, b) -> a.add(b));
            model.addAttribute("stackedHbar", MvcUtils.getHBarFormatted(stackingHbarAmount));

            final BigDecimal stackingEurAmount = stackingHbarAmount.multiply(exchangeRate);
            model.addAttribute("stackedEur", MvcUtils.getEurFormatted(stackingEurAmount));
        }
        return "home";
    }
}
