package br.com.petterson.nbassistant.parser;

import br.com.petterson.nbassistant.model.DocumentFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParserFactory {

    private final PdfParser pdfParser;
    private final DocxParser docxParser;
    private final CsvParser csvParser;
    private final MarkdownParser markdownParser;
    private final XlsxParser xlsxParser;
    private final PptxParser pptxParser;
    private final HtmlParser htmlParser;
    private final JsonParser jsonParser;

    public DocumentParser getParser(DocumentFormat format) {
        return switch (format) {
            case PDF -> pdfParser;
            case DOCX -> docxParser;
            case CSV -> csvParser;
            case MARKDOWN -> markdownParser;
            case XLSX -> xlsxParser;
            case PPTX -> pptxParser;
            case HTML -> htmlParser;
            case JSON -> jsonParser;
            default -> throw new UnsupportedOperationException(
                    "Parser ainda não implementado para o formato: " + format);
        };
    }
}
