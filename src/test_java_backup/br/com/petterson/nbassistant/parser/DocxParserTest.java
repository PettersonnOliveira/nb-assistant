package br.com.petterson.nbassistant.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocxParserTest {

    private final DocxParser parser = new DocxParser();

    @Test
    void shouldParseDocxParagraphsIntoBlocks() throws Exception {
        byte[] docxBytes = createDocx("Primeiro paragrafo", "Segundo paragrafo");

        var file = new MockMultipartFile(
                "file",
                "manual.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes
        );

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).content()).isEqualTo("Primeiro paragrafo");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Parágrafo 1");
        assertThat(parsed.blocks().get(1).content()).isEqualTo("Segundo paragrafo");
        assertThat(parsed.blocks().get(1).locationLabel()).isEqualTo("Parágrafo 2");
    }

    private byte[] createDocx(String... paragraphs) throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String paragraph : paragraphs) {
                document.createParagraph().createRun().setText(paragraph);
            }
            document.write(out);
            return out.toByteArray();
        }
    }
}
