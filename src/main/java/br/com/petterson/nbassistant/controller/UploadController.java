package br.com.petterson.nbassistant.controller;

import br.com.petterson.nbassistant.dto.DocumentResponse;
import br.com.petterson.nbassistant.model.DocumentCategory;
import br.com.petterson.nbassistant.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") DocumentCategory category) {

        DocumentResponse response = uploadService.upload(file, category);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<java.util.List<DocumentResponse>> listAll() {
        return ResponseEntity.ok(java.util.List.of()); // Simplified for brevity, not used by end users
    }
}