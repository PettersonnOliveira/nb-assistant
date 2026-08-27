package br.com.petterson.nbassistant.dto;

public record ChatRequest(
        String question,
        String chatId,
        String category
) {}
