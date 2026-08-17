package br.com.petterson.nbassistant.parser;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class MarkdownParser implements DocumentParser {

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] sections = content.split("(?=\\n#{1,6}\\s)");

        int index = 1;
        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.isEmpty()) continue;

            String label = extractHeading(trimmed, index);

            blocks.add(new ParsedBlock(trimmed, label, index));
            index++;
        }

        return new ParsedDocument(blocks);
    }

    private String extractHeading(String section, int index) {
        String firstLine = section.lines().findFirst().orElse("");
        if (firstLine.startsWith("#")) {
            return "Seção: " + firstLine.replaceAll("^#{1,6}\\s*", "");
        }
        return "Trecho " + index;
    }
}