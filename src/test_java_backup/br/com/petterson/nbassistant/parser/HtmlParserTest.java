package br.com.petterson.nbassistant.parser;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlParserTest {

    private final HtmlParser parser = new HtmlParser();

    @Test
    void shouldParseHtmlSectionsIntoBlocks() {
        String html = """
                <html>
                <body>
                    <h1>Politica de Seguranca</h1>
                    <p>Todos os colaboradores devem seguir as regras.</p>
                    <p>Senhas devem ter no minimo 12 caracteres.</p>
                    <h2>Acesso Remoto</h2>
                    <p>VPN obrigatoria para acessos externos.</p>
                </body>
                </html>
                """;

        var file = new MockMultipartFile("file", "politica.html", "text/html", html.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(3);

        assertThat(parsed.blocks().get(0).content()).contains("colaboradores devem seguir");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Politica de Seguranca");
        assertThat(parsed.blocks().get(0).order()).isEqualTo(1);

        assertThat(parsed.blocks().get(1).content()).contains("12 caracteres");
        assertThat(parsed.blocks().get(1).locationLabel()).isEqualTo("Politica de Seguranca");
        assertThat(parsed.blocks().get(1).order()).isEqualTo(2);

        assertThat(parsed.blocks().get(2).content()).contains("VPN obrigatoria");
        assertThat(parsed.blocks().get(2).locationLabel()).isEqualTo("Acesso Remoto");
        assertThat(parsed.blocks().get(2).order()).isEqualTo(3);
    }

    @Test
    void shouldRemoveScriptAndStyleTags() {
        String html = """
                <html>
                <body>
                    <script>alert('xss');</script>
                    <style>body { color: red; }</style>
                    <nav>Menu principal</nav>
                    <h1>Conteudo Real</h1>
                    <p>Informacao importante.</p>
                    <footer>Rodape</footer>
                </body>
                </html>
                """;

        var file = new MockMultipartFile("file", "limpo.html", "text/html", html.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content()).contains("Informacao importante");
        assertThat(parsed.fullText()).doesNotContain("alert", "color: red", "Menu principal", "Rodape");
    }

    @Test
    void shouldParseListItemsIntoBlocks() {
        String html = """
                <html>
                <body>
                    <h2>Checklist</h2>
                    <ul>
                        <li>Verificar identidade</li>
                        <li>Confirmar endereco</li>
                    </ul>
                </body>
                </html>
                """;

        var file = new MockMultipartFile("file", "lista.html", "text/html", html.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).content()).contains("Verificar identidade");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Checklist");
        assertThat(parsed.blocks().get(1).content()).contains("Confirmar endereco");
    }

    @Test
    void shouldFallbackToBodyTextWhenNoStructuredTags() {
        String html = """
                <html>
                <body>
                    Texto simples sem tags estruturais de heading ou paragrafo.
                </body>
                </html>
                """;

        var file = new MockMultipartFile("file", "simples.html", "text/html", html.getBytes());

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(1);
        assertThat(parsed.blocks().get(0).content()).contains("Texto simples");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Corpo HTML");
    }
}
