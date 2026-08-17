package br.com.petterson.nbassistant.parser;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser implements DocumentParser {

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> headerFields = parseCsvLine(reader.readLine());
            if (headerFields.isEmpty()) {
                return new ParsedDocument(blocks);
            }

            String[] columns = headerFields.toArray(String[]::new);
            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                List<String> values = parseCsvLine(line);
                StringBuilder rowText = new StringBuilder();

                for (int i = 0; i < columns.length && i < values.size(); i++) {
                    rowText.append(columns[i].trim())
                            .append(": ")
                            .append(values.get(i).trim())
                            .append("; ");
                }

                blocks.add(new ParsedBlock(
                        rowText.toString().trim(),
                        "Linha " + (rowNumber + 1),
                        rowNumber
                ));
                rowNumber++;
            }
        }

        return new ParsedDocument(blocks);
    }

    static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        if (line == null) {
            return fields;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        fields.add(current.toString());
        return fields;
    }
}
