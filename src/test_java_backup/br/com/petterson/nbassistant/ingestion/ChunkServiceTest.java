package br.com.petterson.nbassistant.ingestion;

import br.com.petterson.nbassistant.parser.ParsedBlock;
import br.com.petterson.nbassistant.parser.ParsedDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkServiceTest {

    private final ChunkService chunkService = new ChunkService();

    @Test
    void shouldKeepShortTextAsSingleChunk() {
        ParsedDocument document = new ParsedDocument(List.of(
                new ParsedBlock("Texto curto", "Seção 1", 3)
        ));

        List<Chunk> chunks = chunkService.chunk(document);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).content()).isEqualTo("Texto curto");
        assertThat(chunks.get(0).locationLabel()).isEqualTo("Seção 1");
        assertThat(chunks.get(0).blockOrder()).isEqualTo(3);
        assertThat(chunks.get(0).chunkIndex()).isZero();
    }

    @Test
    void shouldSplitLongTextWithOverlap() {
        String longText = "a".repeat(900);
        ParsedDocument document = new ParsedDocument(List.of(
                new ParsedBlock(longText, "Bloco longo", 1)
        ));

        List<Chunk> chunks = chunkService.chunk(document);

        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.get(0).blockOrder()).isEqualTo(1);
        assertThat(chunks.get(1).chunkIndex()).isEqualTo(1);
    }
}
