package br.com.petterson.nbassistant.dto;

public record SourceReference(
        String fileName,
        String location,
        double similarityScore
) {}