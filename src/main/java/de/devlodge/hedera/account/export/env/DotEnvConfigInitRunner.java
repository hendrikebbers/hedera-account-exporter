package de.devlodge.hedera.account.export.env;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotEnvConfigInitRunner implements SpringApplicationRunListener {

    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        environment.getPropertySources().addLast(new DotEnvPropertySource());
    }
}
