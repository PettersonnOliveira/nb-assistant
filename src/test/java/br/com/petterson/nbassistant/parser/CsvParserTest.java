package br.com.petterson.nbassistant.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParserTest {

    private final CsvParser parser = new CsvParser();

    @Test
    void shouldParseCsvRowsIntoBlocks() {
        var file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "pessoas.csv",
                "text/csv",
                """
                        nome,idade,cargo
                        Ana,28,Analista
                        Bruno,31,Gerente
                        """.getBytes()
        );

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).content()).contains("nome: Ana", "idade: 28", "cargo: Analista");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Linha 2");
        assertThat(parsed.blocks().get(1).content()).contains("nome: Bruno", "idade: 31", "cargo: Gerente");
    }

    @Test
    void shouldHandleQuotedFieldsWithCommas() {
        var file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "clientes.csv",
                "text/csv",
                """
                        nome,cidade
                        "Silva, João","São Paulo, SP"
                        """.getBytes()
        );

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content())
                .contains("nome: Silva, João", "cidade: São Paulo, SP");
    }

    @Test
    void shouldParseQuotedLineHelper() {
        List<String> fields = CsvParser.parseCsvLine("\"a,b\",c,\"d\"\"e\"");

        assertThat(fields).containsExactly("a,b", "c", "d\"e");
    }
}
