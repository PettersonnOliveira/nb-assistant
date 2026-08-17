package br.com.petterson.nbassistant.parser;

import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class PdfParser implements DocumentParser {

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            int totalPages = document.getNumberOfPages();

            for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String pageText = stripper.getText(document).trim();

                if (!pageText.isEmpty()) {
                    blocks.add(new ParsedBlock(
                            pageText,
                            "Página " + pageNumber,
                            pageNumber
                    ));
                }
            }
        }

        return new ParsedDocument(blocks);
    }
}