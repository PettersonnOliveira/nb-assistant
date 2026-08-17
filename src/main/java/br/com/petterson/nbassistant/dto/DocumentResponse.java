package br.com.petterson.nbassistant.dto;

import br.com.petterson.nbassistant.model.DocumentCategory;
import br.com.petterson.nbassistant.model.DocumentFormat;
import br.com.petterson.nbassistant.model.DocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String originalFileName,
        DocumentFormat format,
        DocumentCategory category,
        DocumentStatus status,
        LocalDateTime uploadedAt
) {}
