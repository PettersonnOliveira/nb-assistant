package br.com.petterson.nbassistant.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class JsonParser implements DocumentParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            JsonNode rootNode = mapper.readTree(is);
            extractNodes(rootNode, "", blocks, new int[]{1});
        }

        return new ParsedDocument(blocks);
    }

    private void extractNodes(JsonNode node, String prefix, List<ParsedBlock> blocks, int[] counter) {
        if (node.isObject()) {
            List<String> leafEntries = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                String path = prefix.isEmpty() ? key : prefix + "." + key;

                if (value.isValueNode()) {
                    String textValue = value.asText().trim();
                    if (!textValue.isEmpty()) {
                        leafEntries.add(path + ": " + textValue);
                    }
                } else {
                    extractNodes(value, path, blocks, counter);
                }
            }

            if (!leafEntries.isEmpty()) {
                String label = prefix.isEmpty() ? "JSON raiz" : "JSON Path: " + prefix;
                blocks.add(new ParsedBlock(
                        String.join("; ", leafEntries),
                        label,
                        counter[0]++
                ));
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                extractNodes(node.get(i), prefix + "[" + i + "]", blocks, counter);
            }
        } else {
            String value = node.asText().trim();
            if (!value.isEmpty()) {
                String text = prefix.isEmpty() ? value : prefix + ": " + value;
                blocks.add(new ParsedBlock(
                        text,
                        prefix.isEmpty() ? "JSON raiz" : "JSON Path: " + prefix,
                        counter[0]++
                ));
            }
        }
    }
}
