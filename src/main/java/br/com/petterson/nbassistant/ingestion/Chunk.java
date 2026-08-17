package br.com.petterson.nbassistant.ingestion;

public record Chunk(
        String content,
        String locationLabel,
        int blockOrder,
        int chunkIndex
) {}
