package br.com.petterson.nbassistant.service;

import br.com.petterson.nbassistant.dto.DashboardResponse;
import br.com.petterson.nbassistant.model.DocumentCategory;
import br.com.petterson.nbassistant.model.DocumentStatus;
import br.com.petterson.nbassistant.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DocumentRepository documentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ChatMetricsService chatMetricsService;

    public DashboardResponse getDashboard() {
        long total = documentRepository.count();
        long processed = documentRepository.countByStatus(DocumentStatus.PROCESSED);
        long failed = documentRepository.countByStatus(DocumentStatus.FAILED);
        long pending = documentRepository.countByStatus(DocumentStatus.PENDING)
                + documentRepository.countByStatus(DocumentStatus.PROCESSING);

        Long totalChunks = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store", Long.class);
        long chunks = totalChunks != null ? totalChunks : 0;

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (DocumentCategory category : DocumentCategory.values()) {
            long count = documentRepository.findByCategory(category).size();
            if (count > 0) {
                byCategory.put(category.name(), count);
            }
        }

        return new DashboardResponse(
                total,
                processed,
                failed,
                pending,
                chunks,
                chunks,
                chatMetricsService.getTotalQuestions(),
                byCategory
        );
    }
}