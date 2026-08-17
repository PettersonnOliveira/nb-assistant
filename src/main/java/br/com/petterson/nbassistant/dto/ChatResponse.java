package br.com.petterson.nbassistant.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceReference> sources
) {}