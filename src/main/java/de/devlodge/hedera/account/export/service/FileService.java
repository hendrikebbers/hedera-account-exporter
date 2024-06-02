package de.devlodge.hedera.account.export.service;

import java.io.File;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private final Path basePath;


    public FileService() {
        basePath = Path.of(System.getProperty("user.home") + File.separator + ".crypto-export");
    }

    public Path getBasePath() {
        return basePath;
    }

    public Path getPathForFile(String file) {
        return basePath.resolve(file);
    }
}
