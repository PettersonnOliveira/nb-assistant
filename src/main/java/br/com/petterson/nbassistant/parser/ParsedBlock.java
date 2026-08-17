package br.com.petterson.nbassistant.parser;

public record ParsedBlock(
        String content,
        String locationLabel,
        int order
) {}