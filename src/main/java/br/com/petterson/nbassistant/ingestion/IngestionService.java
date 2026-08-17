package br.com.petterson.nbassistant.ingestion;

import br.com.petterson.nbassistant.model.Document;
import br.com.petterson.nbassistant.model.DocumentStatus;
import br.com.petterson.nbassistant.parser.ParsedDocument;
import br.com.petterson.nbassistant.parser.ParserFactory;
import br.com.petterson.nbassistant.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ParserFactory parserFactory;
    private final DocumentRepository documentRepository;
    private final ChunkService chunkService;
    private final EmbeddingService embeddingService;

    public void ingest(Document document, MultipartFile file) {
        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            var parser = parserFactory.getParser(document.getFormat());
            ParsedDocument parsed = parser.parse(file);

            if (parsed.blocks().isEmpty()) {
                throw new IllegalStateException(
                        "Nenhum conteúdo extraído do arquivo: " + document.getOriginalFileName());
            }

            List<Chunk> chunks = chunkService.chunk(parsed);

            if (chunks.isEmpty()) {
                throw new IllegalStateException(
                        "Nenhum chunk gerado para o arquivo: " + document.getOriginalFileName());
            }

            embeddingService.embedAndStore(document, chunks);

            document.setStatus(DocumentStatus.PROCESSED);
            document.setChunkCount(chunks.size());
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);

            log.info("Documento '{}' processado com sucesso: {} chunks indexados",
                    document.getOriginalFileName(), chunks.size());

        } catch (Exception e) {
            document.setStatus(DocumentStatus.FAILED);
            document.setFailureReason(e.getMessage());
            documentRepository.save(document);
            log.error("Falha ao processar documento '{}': {}",
                    document.getOriginalFileName(), e.getMessage(), e);
        }
    }
}