package br.com.petterson.nbassistant.dto;

import java.util.Map;

public record DashboardResponse(
        long totalDocuments,
        long processedDocuments,
        long failedDocuments,
        long pendingDocuments,
        long totalChunks,
        long totalEmbeddings,
        long totalQuestions,
        Map<String, Long> documentsByCategory
) {}