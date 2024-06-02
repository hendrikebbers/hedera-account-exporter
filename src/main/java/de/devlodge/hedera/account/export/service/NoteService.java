package de.devlodge.hedera.account.export.service;

import de.devlodge.hedera.account.export.model.Transaction;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final Path path;

    private final Properties notes;

    public NoteService(final FileService fileService) {
        this.path = fileService.getPathForFile("notes.properties");
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
        try {
            final String originalString =
                    transaction.networkId() + "-" + transaction.timestamp() + "-" + transaction.amount();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
            return new String(encodedhash, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not hash note", e);
        }
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
