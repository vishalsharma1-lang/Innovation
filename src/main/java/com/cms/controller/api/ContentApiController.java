package com.cms.controller.api;

import com.cms.entity.ContentSettings;
import com.cms.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ContentApiController {

    @Autowired
    private ContentService contentService;

    /** GET /content/{page} — fetch all active content for a page */
    @GetMapping("/content/{page}")
    public ResponseEntity<?> getContentByPage(@PathVariable String page) {
        List<ContentSettings> content = contentService.getContentByPage(page);
        if (content.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No content found for page: " + page, "data", content));
        }
        return ResponseEntity.ok(content);
    }

    /** POST /content — create new content */
    @PostMapping("/content")
    public ResponseEntity<?> createContent(@RequestBody ContentSettings content) {
        try {
            ContentSettings saved = contentService.createOrUpdateContent(content);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /content/{id} — update content by ID */
    @PutMapping("/content/{id}")
    public ResponseEntity<?> updateContent(@PathVariable Long id, @RequestBody ContentSettings content) {
        return contentService.getContentById(id).map(existing -> {
            content.setId(id);
            ContentSettings updated = contentService.saveContent(content);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /content/{id} */
    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        return contentService.getContentById(id).map(content -> {
            contentService.deleteContent(id);
            return ResponseEntity.ok(Map.of("message", "Content deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
