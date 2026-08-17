package br.com.petterson.nbassistant.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile file, String destinationPath);
    byte[] download(String storagePath);
    void delete(String storagePath);
}