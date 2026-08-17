package br.com.petterson.nbassistant.parser;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PptxParserTest {

    private final PptxParser parser = new PptxParser();

    @Test
    void shouldParseSlidesWithTextIntoBlocks() throws Exception {
        byte[] pptxBytes = createPptx(ppt -> {
            XSLFSlide slide1 = ppt.createSlide();
            slide1.createTextBox().setText("Boas-vindas a NB.CASH");

            XSLFSlide slide2 = ppt.createSlide();
            slide2.createTextBox().setText("Nossos produtos financeiros");
        });

        var file = new MockMultipartFile("file", "apresentacao.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSizeGreaterThanOrEqualTo(2);

        assertThat(parsed.blocks().stream().map(ParsedBlock::content))
                .anyMatch(c -> c.contains("Boas-vindas a NB.CASH"));
        assertThat(parsed.blocks().stream().map(ParsedBlock::content))
                .anyMatch(c -> c.contains("Nossos produtos financeiros"));

        assertThat(parsed.blocks().get(0).locationLabel()).isEqualTo("Slide 1");
    }

    @Test
    void shouldSkipEmptySlides() throws Exception {
        byte[] pptxBytes = createPptx(ppt -> {
            ppt.createSlide(); // slide vazio
            XSLFSlide slide2 = ppt.createSlide();
            slide2.createTextBox().setText("Conteudo do segundo slide");
        });

        var file = new MockMultipartFile("file", "com-vazio.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(parsed.blocks().stream().map(ParsedBlock::content))
                .anyMatch(c -> c.contains("Conteudo do segundo slide"));
    }

    @Test
    void shouldHandleSingleSlide() throws Exception {
        byte[] pptxBytes = createPptx(ppt -> {
            XSLFSlide slide = ppt.createSlide();
            slide.createTextBox().setText("Slide unico com informacoes de compliance");
        });

        var file = new MockMultipartFile("file", "unico.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes);

        ParsedDocument parsed = parser.parse(file);

        assertThat(parsed.blocks()).isNotEmpty();
        assertThat(parsed.blocks().stream().map(ParsedBlock::content))
                .anyMatch(c -> c.contains("compliance"));
    }

    // --- helper ---

    private byte[] createPptx(java.util.function.Consumer<XMLSlideShow> builder) throws Exception {
        try (var ppt = new XMLSlideShow(); var out = new ByteArrayOutputStream()) {
            builder.accept(ppt);
            ppt.write(out);
            return out.toByteArray();
        }
    }
}
