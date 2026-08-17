package br.com.petterson.nbassistant.storage;

import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final String BASE_DIR = "storage-local";

    @Override
    @SneakyThrows
    public String upload(MultipartFile file, String destinationPath) {
        Path target = Path.of(BASE_DIR, destinationPath);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    @Override
    @SneakyThrows
    public byte[] download(String storagePath) {
        return Files.readAllBytes(Path.of(storagePath));
    }

    @Override
    public void delete(String storagePath) {
        new File(storagePath).delete();
    }
}