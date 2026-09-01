package br.com.petterson.nbassistant.initializer;

import br.com.petterson.nbassistant.ingestion.IngestionService;
import br.com.petterson.nbassistant.model.Document;
import br.com.petterson.nbassistant.model.DocumentCategory;
import br.com.petterson.nbassistant.model.DocumentFormat;
import br.com.petterson.nbassistant.model.DocumentStatus;
import br.com.petterson.nbassistant.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentInitializer {

    private final DocumentRepository documentRepository;
    private final IngestionService ingestionService;

    private static final String STORAGE_DIR = "storage-local";
    private static final long INGEST_INTERVAL_MS = 3000;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDocuments() throws Exception {
        log.info("Iniciando DocumentInitializer para carregar documentos pré-cadastrados...");

        Path storagePath = Paths.get(STORAGE_DIR);
        if (!Files.exists(storagePath)) {
            log.warn("Diretório {} não encontrado. Pulando inicialização.", STORAGE_DIR);
            return;
        }

        File[] categoryDirs = storagePath.toFile().listFiles(File::isDirectory);
        if (categoryDirs == null || categoryDirs.length == 0) {
            log.warn("Nenhuma categoria encontrada em {}. Pulando inicialização.", STORAGE_DIR);
            return;
        }

        int processedCount = 0;
        int skippedCount = 0;

        for (File categoryDir : categoryDirs) {
            String categoryName = categoryDir.getName();
            
            try {
                DocumentCategory category = DocumentCategory.valueOf(categoryName);
                log.info("Processando categoria: {}", categoryName);

                File[] files = categoryDir.listFiles(File::isFile);
                if (files == null || files.length == 0) {
                    log.info("Nenhum arquivo encontrado em {}", categoryName);
                    continue;
                }

                for (File file : files) {
                    String fileName = file.getName();
                    
                    // Verificar se documento já existe e está processado
                    Optional<Document> existingDoc = documentRepository.findByCategory(category).stream()
                            .filter(d -> d.getOriginalFileName().equals(fileName) && d.getStatus() == DocumentStatus.PROCESSED)
                            .findFirst();

                    if (existingDoc.isPresent()) {
                        log.info("Documento {} já processado. Pulando.", fileName);
                        skippedCount++;
                        continue;
                    }

                    try {
                        // Criar documento
                        DocumentFormat format = resolveFormat(fileName);
                        
                        Document document = Document.builder()
                                .originalFileName(fileName)
                                .format(format)
                                .category(category)
                                .status(DocumentStatus.PENDING)
                                .objectStoragePath(file.getAbsolutePath())
                                .build();

                        document = documentRepository.save(document);
                        log.info("Documento criado: {} (ID: {})", fileName, document.getId());

                        // Converter File para MultipartFile
                        MultipartFile multipartFile = convertToMultipartFile(file);

                        // Processar documento
                        ingestionService.ingest(document, multipartFile);
                        processedCount++;
                        Thread.sleep(INGEST_INTERVAL_MS);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Inicialização interrompida durante o intervalo entre documentos.");
                        return;

                    } catch (Exception e) {
                        log.error("Erro ao processar arquivo {}: {}. Documento marcado como FAILED.", fileName, e.getMessage());
                        // Marcar documento como FAILED se ocorrer erro
                        try {
                            Optional<Document> failedDoc = documentRepository.findByCategory(category).stream()
                                    .filter(d -> d.getOriginalFileName().equals(fileName))
                                    .findFirst();
                            if (failedDoc.isPresent()) {
                                Document doc = failedDoc.get();
                                doc.setStatus(DocumentStatus.FAILED);
                                doc.setFailureReason(e.getMessage());
                                documentRepository.save(doc);
                            }
                        } catch (Exception ex) {
                            log.error("Erro ao marcar documento como FAILED: {}", ex.getMessage());
                        }
                    }
                }

            } catch (IllegalArgumentException e) {
                log.warn("Categoria {} não existe no enum DocumentCategory. Pulando.", categoryName);
            }
        }

        log.info("DocumentInitializer concluído. Processados: {}, Pulados: {}", processedCount, skippedCount);
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

    private MultipartFile convertToMultipartFile(File file) throws Exception {
        return new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return file.getName();
            }

            @Override
            public String getContentType() {
                try {
                    return Files.probeContentType(file.toPath());
                } catch (Exception e) {
                    return "application/octet-stream";
                }
            }

            @Override
            public boolean isEmpty() {
                return file.length() == 0;
            }

            @Override
            public long getSize() {
                return file.length();
            }

            @Override
            public byte[] getBytes() throws java.io.IOException {
                return Files.readAllBytes(file.toPath());
            }

            @Override
            public java.io.InputStream getInputStream() throws java.io.IOException {
                return new FileInputStream(file);
            }

            @Override
            public void transferTo(java.io.File dest) throws java.io.IOException, IllegalStateException {
                Files.copy(file.toPath(), dest.toPath());
            }
        };
    }
}