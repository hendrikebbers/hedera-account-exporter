package de.devlodge.hedera.account.export;

import de.devlodge.hedera.account.export.clients.CoinBaseClient;
import de.devlodge.hedera.account.export.clients.ExchangeClient;
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
