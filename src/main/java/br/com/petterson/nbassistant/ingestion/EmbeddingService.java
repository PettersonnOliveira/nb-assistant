package br.com.petterson.nbassistant.ingestion;

import br.com.petterson.nbassistant.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;

    public void embedAndStore(Document document, List<Chunk> chunks) {
        List<org.springframework.ai.document.Document> aiDocuments = chunks.stream()
                .map(chunk -> new org.springframework.ai.document.Document(
                        chunk.content(),
                        Map.of(
                                "documentId", document.getId().toString(),
                                "fileName", document.getOriginalFileName(),
                                "category", document.getCategory().name(),
                                "location", chunk.locationLabel(),
                                "blockOrder", chunk.blockOrder(),
                                "chunkIndex", chunk.chunkIndex()
                        )
                ))
                .collect(Collectors.toList());

        vectorStore.add(aiDocuments);

        log.info("Documento '{}' indexado com {} chunks no vector store",
                document.getOriginalFileName(), aiDocuments.size());
    }
}