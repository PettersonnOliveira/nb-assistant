package br.com.petterson.nbassistant.parser;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownParserTest {

    private final MarkdownParser parser = new MarkdownParser();

    @Test
    void shouldParseMarkdownSectionsIntoBlocks() {
        var file = new MockMultipartFile(
                "file",
                "manual.md",
                "text/markdown",
                """
                        # Introducao
                        Este e o texto inicial.

                        ## Regras
                        Primeira regra.
                        Segunda regra.
                        """.getBytes()
        );

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Seção: Introducao");
        assertThat(parsed.blocks().get(0).order()).isEqualTo(1);
        assertThat(parsed.blocks().get(0).content()).contains("Introducao", "texto inicial");
        assertThat(parsed.blocks().get(1).locationLabel()).isEqualTo("Seção: Regras");
        assertThat(parsed.blocks().get(1).order()).isEqualTo(2);
        assertThat(parsed.blocks().get(1).content()).contains("Primeira regra", "Segunda regra");
    }
}
