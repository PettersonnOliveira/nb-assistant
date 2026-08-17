package br.com.petterson.nbassistant.ingestion;

import br.com.petterson.nbassistant.parser.ParsedBlock;
import br.com.petterson.nbassistant.parser.ParsedDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final int CHUNK_SIZE = 700;
    private static final int OVERLAP = 100;

    public List<Chunk> chunk(ParsedDocument parsedDocument) {
        List<Chunk> chunks = new ArrayList<>();
        int globalIndex = 0;

        for (ParsedBlock block : parsedDocument.blocks()) {
            List<String> pieces = splitWithOverlap(block.content(), CHUNK_SIZE, OVERLAP);

            for (String piece : pieces) {
                chunks.add(new Chunk(piece, block.locationLabel(), block.order(), globalIndex++));
            }
        }

        return chunks;
    }

    private List<String> splitWithOverlap(String text, int size, int overlap) {
        List<String> pieces = new ArrayList<>();

        if (text.length() <= size) {
            pieces.add(text);
            return pieces;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            pieces.add(text.substring(start, end));

            if (end == text.length()) break;
            start = end - overlap;
        }

        return pieces;
    }
}