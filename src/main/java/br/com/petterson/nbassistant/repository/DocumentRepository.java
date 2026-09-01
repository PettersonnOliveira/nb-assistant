package br.com.petterson.nbassistant.repository;

import br.com.petterson.nbassistant.model.Document;
import br.com.petterson.nbassistant.model.DocumentCategory;
import br.com.petterson.nbassistant.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    long countByStatus(DocumentStatus status);

    List<Document> findByCategory(DocumentCategory category);

    long countByCategory(DocumentCategory category);

    // 💡 Otimização: Conta direto no banco sem carregar objetos para a memória Java
    long countByCategoryAndStatus(DocumentCategory category, DocumentStatus status);
}