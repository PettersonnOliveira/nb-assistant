package br.com.petterson.nbassistant.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocxParser implements DocumentParser {

    @Override
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {

            int paragraphNumber = 1;

            for (XWPFParagraph paragraph : document.getParagraphs()) {

                String text = paragraph.getText().trim();

                if (!text.isEmpty()) {
                    blocks.add(new ParsedBlock(
                            text,
                            "Parágrafo " + paragraphNumber,
                            paragraphNumber
                    ));
                }

                paragraphNumber++;
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao extrair texto do arquivo Word: " + file.getOriginalFilename(),
                    e
            );
        }

        return new ParsedDocument(blocks);
    }
}
