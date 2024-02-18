package de.devlodge.hedera.account.export.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final Path path;

    private final Properties notes;

    public NoteService(@Value("${noteFile}") final String noteFile) {
        this.path = Path.of(noteFile);
        this.notes = new Properties();
        load();
    }

    public void addNote(final String hash, final String note) {
        Objects.requireNonNull(hash, "hash must not be null");
        notes.put(hash, note);
        save();
    }

    public Optional<String> getNote(final String hash) {
        Objects.requireNonNull(hash, "hash must not be null");
        return Optional.ofNullable(notes.getProperty(hash));
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
        try(final OutputStream outputStream = Files.newOutputStream(path, java.nio.file.StandardOpenOption.CREATE, StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING)) {
            notes.store(outputStream, comment);
        } catch (Exception e) {
            throw new RuntimeException("Could not save notes", e);
        }
    }

}
