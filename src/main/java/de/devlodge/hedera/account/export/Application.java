package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.exchange.coinbase.CoinBaseClient;
import de.devlodge.hedera.account.export.exchange.ExchangeClient;
import de.devlodge.hedera.account.export.ledgers.LedgerClient;
import de.devlodge.hedera.account.export.ledgers.hedera.HederaClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ExchangeClient exchangeClient() {
        return new CoinBaseClient();
    }

}
