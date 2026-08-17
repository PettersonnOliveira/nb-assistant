package br.com.petterson.nbassistant.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfParserTest {

    private final PdfParser parser = new PdfParser();

    @Test
    void shouldParsePdfPagesIntoBlocks() throws Exception {
        byte[] pdfBytes = createPdf("Manual NB.CASH pagina 1", "Politica interna pagina 2");

        var file = new MockMultipartFile("file", "manual.pdf", "application/pdf", pdfBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSize(2);
        assertThat(parsed.blocks().get(0).content()).contains("Manual NB.CASH");
        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Página 1");
        assertThat(parsed.blocks().get(1).content()).contains("Politica interna");
        assertThat(parsed.blocks().get(1).locationLabel()).isEqualTo("Página 2");
    }

    private byte[] createPdf(String... pageTexts) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (String pageText : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(font, 12);
                    content.newLineAtOffset(50, 700);
                    content.showText(pageText);
                    content.endText();
                }
            }
            document.save(out);
            return out.toByteArray();
        }
    }
}
