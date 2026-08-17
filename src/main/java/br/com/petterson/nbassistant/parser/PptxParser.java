package br.com.petterson.nbassistant.parser;

import lombok.SneakyThrows;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class PptxParser implements DocumentParser {

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(is)) {

            int slideNumber = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                StringBuilder slideText = new StringBuilder();

                for (XSLFTextShape shape : slide.getShapes().stream()
                        .filter(s -> s instanceof XSLFTextShape)
                        .map(s -> (XSLFTextShape) s)
                        .toList()) {
                    String shapeText = shape.getText().trim();
                    if (!shapeText.isEmpty()) {
                        slideText.append(shapeText).append("\n");
                    }
                }

                String text = slideText.toString().trim();
                if (!text.isEmpty()) {
                    blocks.add(new ParsedBlock(
                            text,
                            "Slide " + slideNumber,
                            slideNumber
                    ));
                }
                slideNumber++;
            }
        }

        return new ParsedDocument(blocks);
    }
}
