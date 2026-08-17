package br.com.petterson.nbassistant.parser;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParser implements DocumentParser {

    @Override
    @SneakyThrows
    public ParsedDocument parse(MultipartFile file) {
        List<ParsedBlock> blocks = new ArrayList<>();

        String html = new String(file.getBytes(), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);

        // Remover scripts e styles do contexto
        doc.select("script, style, nav, footer, header").remove();

        Elements elements = doc.body().select("h1, h2, h3, h4, h5, h6, p, li, td, th, blockquote");

        int blockIndex = 1;
        String currentSection = "Seção Inicial";

        for (Element element : elements) {
            if (element.tagName().matches("h[1-6]")) {
                currentSection = element.text();
            } else {
                String text = element.text().trim();
                if (!text.isEmpty()) {
                    blocks.add(new ParsedBlock(
                            text,
                            currentSection,
                            blockIndex++
                    ));
                }
            }
        }

        // Se a página não tiver estrutura clara de tags, tenta pegar o texto puro
        if (blocks.isEmpty()) {
            String text = doc.body().text().trim();
            if (!text.isEmpty()) {
                blocks.add(new ParsedBlock(text, "Corpo HTML", blockIndex));
            }
        }

        return new ParsedDocument(blocks);
    }
}