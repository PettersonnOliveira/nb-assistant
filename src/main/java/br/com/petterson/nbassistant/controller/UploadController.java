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

    @GetMapping("/categories")
    public ResponseEntity<java.util.List<CategoryInfo>> listCategories(
            @org.springframework.beans.factory.annotation.Autowired br.com.petterson.nbassistant.repository.DocumentRepository documentRepository) {
        
        java.util.List<CategoryInfo> categories = new java.util.ArrayList<>();

        for (DocumentCategory category : DocumentCategory.values()) {
            long count = documentRepository.findByCategory(category).stream()
                    .filter(d -> d.getStatus() == br.com.petterson.nbassistant.model.DocumentStatus.PROCESSED)
                    .count();
            if (count > 0) {
                categories.add(new CategoryInfo(
                        category.name(),
                        category.getDisplayName(),
                        category.getEmoji(),
                        count
                ));
            }
        }

        return ResponseEntity.ok(categories);
    }

    public record CategoryInfo(
            String name,
            String displayName,
            String emoji,
            long documentCount
    ) {}
}