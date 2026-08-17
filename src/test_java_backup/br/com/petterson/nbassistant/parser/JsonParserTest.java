package br.com.petterson.nbassistant.parser;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class JsonParserTest {

    private final JsonParser parser = new JsonParser();

    @Test
    void shouldParseFlatJsonObjectIntoSingleBlock() {
        String json = """
                {
                    "nome": "Conta Kids",
                    "tipo": "Conta Digital",
                    "idade_minima": "10"
                }
                """;

        var file = new MockMultipartFile("file", "produto.json", "application/json", json.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content())
                .contains("nome: Conta Kids", "tipo: Conta Digital", "idade_minima: 10");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("JSON raiz");
    }

    @Test
    void shouldParseNestedObjectsWithDotNotation() {
        String json = """
                {
                    "produto": {
                        "nome": "Cartao Teen",
                        "limite": "500"
                    }
                }
                """;

        var file = new MockMultipartFile("file", "nested.json", "application/json", json.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content())
                .contains("produto.nome: Cartao Teen", "produto.limite: 500");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("JSON Path: produto");
    }

    @Test
    void shouldParseArraysWithIndexNotation() {
        String json = """
                {
                    "categorias": ["Compliance", "Seguranca", "RH"]
                }
                """;

        var file = new MockMultipartFile("file", "array.json", "application/json", json.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(3);
        assertThat(parsed.blocks().get(0).content()).isEqualTo("categorias[0]: Compliance");
        assertThat(parsed.blocks().get(1).content()).isEqualTo("categorias[1]: Seguranca");
        assertThat(parsed.blocks().get(2).content()).isEqualTo("categorias[2]: RH");
    }

    @Test
    void shouldIgnoreEmptyValues() {
        String json = """
                {
                    "nome": "Pix Instantaneo",
                    "descricao": "",
                    "obs": "Disponivel 24h"
                }
                """;

        var file = new MockMultipartFile("file", "vazio.json", "application/json", json.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content())
                .contains("Pix Instantaneo", "Disponivel 24h")
                .doesNotContain("descricao:");
    }

    @Test
    void shouldParseArrayOfObjects() {
        String json = """
                {
                    "funcionarios": [
                        {"nome": "Ana", "cargo": "Analista"},
                        {"nome": "Bruno", "cargo": "Gerente"}
                    ]
                }
                """;

        var file = new MockMultipartFile("file", "lista.json", "application/json", json.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).content())
                .contains("funcionarios[0].nome: Ana", "funcionarios[0].cargo: Analista");
        assertThat(parsed.blocks().get(1).content())
                .contains("funcionarios[1].nome: Bruno", "funcionarios[1].cargo: Gerente");
    }
}
