package de.devlodge.hedera.account.export.env;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.core.env.PropertySource;

public class DotEnvPropertySource extends PropertySource<Dotenv> {

    public DotEnvPropertySource() {
        super("Dotenv", Dotenv.load());
    }

    @Override
    public Object getProperty(String name) {
        return getSource().get(name);
    }
}
