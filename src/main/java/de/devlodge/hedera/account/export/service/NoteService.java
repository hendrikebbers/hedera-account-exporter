package de.devlodge.hedera.account.export.service;

import de.devlodge.hedera.account.export.model.Transaction;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final Path path;

    private final Properties notes;

    public NoteService() {
        String noteFile = System.getProperty("user.home") + File.separator + ".crypto-export" + File.separator + "notes.properties";
        this.path = Path.of(noteFile);
        this.notes = new Properties();

        load();
    }

    public void addNote(final Transaction transaction, final String note) {
       final String hash = hash(transaction);
        notes.put(hash, note);
        save();
    }

    public Optional<String> getNote(final Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        final String hash = hash(transaction);
        return Optional.ofNullable(notes.getProperty(hash));
    }

    private String hash(final Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        return transaction.hederaTransactionId() + "-" + transaction.timestamp() + "-" + transaction.hbarAmount();
    }

    private void load() {
        if(Files.exists(path)) {
            try (final InputStream inputStream = Files.newInputStream(path, java.nio.file.StandardOpenOption.CREATE, StandardOpenOption.READ)) {
                notes.load(inputStream);
            } catch (Exception e) {
                throw new RuntimeException("Could not load notes", e);
            }
        }
    }

    private void save() {
        final Instant now = Instant.now();
        final String comment = "Created " + now;
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Can not create dir", e);
        }

        try(final OutputStream outputStream = Files.newOutputStream(path, java.nio.file.StandardOpenOption.CREATE, StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            notes.store(outputStream, comment);
        } catch (Exception e) {
            throw new RuntimeException("Could not save notes", e);
        }
    }

}
