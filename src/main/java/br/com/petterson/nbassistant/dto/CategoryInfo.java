package br.com.petterson.nbassistant.dto;

public record CategoryInfo(
        String category,
        String displayName,
        String emoji,
        long documentCount
) {}