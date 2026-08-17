package br.com.petterson.nbassistant.parser;

import java.util.List;

public record ParsedDocument(
        List<ParsedBlock> blocks
) {
    public String fullText() {
        return blocks.stream()
                .map(ParsedBlock::content)
                .reduce("", (a, b) -> a + "\n" + b);
    }
}