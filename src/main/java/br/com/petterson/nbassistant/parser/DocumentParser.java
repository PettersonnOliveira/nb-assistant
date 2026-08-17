package br.com.petterson.nbassistant.parser;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {
    ParsedDocument parse(MultipartFile file);
}