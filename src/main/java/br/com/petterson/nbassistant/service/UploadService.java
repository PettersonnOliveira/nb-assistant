package br.com.petterson.nbassistant.service;

import br.com.petterson.nbassistant.dto.DocumentResponse;
import br.com.petterson.nbassistant.ingestion.IngestionService;
import br.com.petterson.nbassistant.model.*;
import br.com.petterson.nbassistant.repository.DocumentRepository;
import br.com.petterson.nbassistant.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final IngestionService ingestionService;

    public DocumentResponse upload(MultipartFile file, DocumentCategory category) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo não pode estar vazio.");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("O arquivo precisa possuir um nome válido.");
        }

        if (category == null) {
            throw new IllegalArgumentException("A categoria do documento é obrigatória.");
        }

        DocumentFormat format = resolveFormat(file.getOriginalFilename());

        Document document = Document.builder()
                .originalFileName(file.getOriginalFilename())
                .format(format)
                .category(category)
                .status(DocumentStatus.PENDING)
                .build();

        document = documentRepository.save(document);

        String destinationPath = "%s/%s_%s".formatted(
                category.name(),
                document.getId(),
                file.getOriginalFilename()
        );

        String storagePath = storageService.upload(file, destinationPath);

        document.setObjectStoragePath(storagePath);
        document = documentRepository.save(document);

        ingestionService.ingest(document, file);

        return toResponse(document);
    }

    private DocumentFormat resolveFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Arquivo sem extensão válida: " + fileName);
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
        String formatName = switch (extension) {
            case "MD", "MARKDOWN" -> "MARKDOWN";
            case "HTM" -> "HTML";
            default -> extension;
        };
        try {
            return DocumentFormat.valueOf(formatName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato não suportado: " + extension);
        }
    }

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(
                d.getId(), d.getOriginalFileName(), d.getFormat(),
                d.getCategory(), d.getStatus(), d.getUploadedAt());
    }
}