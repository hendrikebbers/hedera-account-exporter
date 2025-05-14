package de.devlodge.hedera.account.export.storage;

import de.devlodge.hedera.account.export.exchange.ExchangePair;
import de.devlodge.hedera.account.export.model.Transaction;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
@ApplicationScope
public class StorageServiceImpl implements StorageService {

    private final static Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    private final Path path;

    private final Properties internalStore;

    private final static ZoneId ZONE_ID = ZoneId.systemDefault();

    public StorageServiceImpl() {
        String noteFile = System.getProperty("user.home") + File.separator + ".crypto-export" + File.separator
                + "notes.properties";
        this.path = Path.of(noteFile);
        this.internalStore = new Properties();
        load();
    }

    public void addNote(final Transaction transaction, final String note) {
        final String hash = hash(transaction);
        internalStore.put(hash, note);
        save();
    }

    public Optional<String> getNote(final Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        final String hash = hash(transaction);
        return Optional.ofNullable(internalStore.getProperty(hash));
    }

    @Override
    public void addExchangeRate(ExchangePair pair, Instant date, BigDecimal rate) {
        Objects.requireNonNull(pair, "pair must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(rate, "rate must not be null");
        final String hash = hash(pair, date);
        internalStore.put(hash + "-exchangeRate", rate.toString());
        log.info("Exchange rate added to store");
        save();
    }

    @Override
    public Optional<BigDecimal> getExchangeRate(ExchangePair pair, Instant date) {
        Objects.requireNonNull(pair, "pair must not be null");
        Objects.requireNonNull(date, "date must not be null");
        final String hash = hash(pair, date);
        final String rate = internalStore.getProperty(hash + "-exchangeRate");
        return Optional.ofNullable(rate).map(BigDecimal::new);
    }

    private String hash(final Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        final String data =
                transaction.networkId() + "-" + transaction.timestamp() + "-" + transaction.amount();
        return hash(data);
    }

    private String hash(final ExchangePair pair, final Instant date) {
        Objects.requireNonNull(pair, "pair must not be null");
        Objects.requireNonNull(date, "date must not be null");

        final String data =
                pair.from() + "-" + pair.to() + "-" + date.atZone(ZONE_ID).toEpochSecond();
        return hash(data);
    }

    private String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return new String(encodedhash, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error in Hashing", e);
        }
    }

    private void load() {
        if (Files.exists(path)) {
            try (final InputStream inputStream = Files.newInputStream(path, java.nio.file.StandardOpenOption.CREATE,
                    StandardOpenOption.READ)) {
                internalStore.load(inputStream);
            } catch (Exception e) {
                throw new RuntimeException("Could not load notes", e);
            }
        }
        log.info("{} key-value pairs loaded for storage", internalStore.size());
    }

    private void save() {
        final Instant now = Instant.now();
        final String comment = "Created " + now;
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Can not create dir", e);
        }

        try (final OutputStream outputStream = Files.newOutputStream(path, java.nio.file.StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            internalStore.store(outputStream, comment);
            log.info("{} key-value pairs stored from storage", internalStore.size());
        } catch (Exception e) {
            throw new RuntimeException("Could not save notes", e);
        }
    }

}
