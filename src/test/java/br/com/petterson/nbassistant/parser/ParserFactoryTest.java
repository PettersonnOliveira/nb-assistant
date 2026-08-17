package br.com.petterson.nbassistant.parser;

import br.com.petterson.nbassistant.model.DocumentFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ParserFactoryTest {

    private final ParserFactory parserFactory = new ParserFactory(
            new PdfParser(),
            new DocxParser(),
            new CsvParser(),
            new MarkdownParser(),
            new XlsxParser(),
            new PptxParser(),
            new HtmlParser(),
            new JsonParser()
    );

    @ParameterizedTest
    @EnumSource(DocumentFormat.class)
    void shouldReturnParserForAllSupportedFormats(DocumentFormat format) {
        DocumentParser parser = parserFactory.getParser(format);

        assertThat(parser).isNotNull();
    }

    @Test
    void shouldMapEachFormatToExpectedParserType() {
        assertThat(parserFactory.getParser(DocumentFormat.PDF)).isInstanceOf(PdfParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.DOCX)).isInstanceOf(DocxParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.CSV)).isInstanceOf(CsvParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.MARKDOWN)).isInstanceOf(MarkdownParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.XLSX)).isInstanceOf(XlsxParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.PPTX)).isInstanceOf(PptxParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.HTML)).isInstanceOf(HtmlParser.class);
        assertThat(parserFactory.getParser(DocumentFormat.JSON)).isInstanceOf(JsonParser.class);
    }
}
