package com.cms.controller.api;

import com.cms.service.UsedCarExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/used-car")
public class UsedCarExtractorApiController {

    @Autowired
    private UsedCarExtractorService extractorService;

    // Extract from pasted HTML/text
    @PostMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractFromText(@RequestBody Map<String, String> body) {
        String content   = body.getOrDefault("content", "").trim();
        String sourceUrl = body.getOrDefault("url", "").trim();

        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Content is required"));
        }
        try {
            UsedCarExtractorService.ExtractionResult result = extractorService.extract(content, sourceUrl);
            if (result.ok()) {
                return ResponseEntity.ok(Map.of("ok", true, "json", result.json()));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "error", result.error()));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // Fetch & extract from URL
    @PostMapping("/extract-url")
    public ResponseEntity<Map<String, Object>> extractFromUrl(@RequestBody Map<String, String> body) {
        String url = body.getOrDefault("url", "").trim();
        if (url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "URL is required"));
        }
        try {
            String content = extractorService.fetchPageContent(url);
            UsedCarExtractorService.ExtractionResult result = extractorService.extract(content, url);
            if (result.ok()) {
                return ResponseEntity.ok(Map.of("ok", true, "json", result.json(), "preview", content.substring(0, Math.min(200, content.length()))));
            } else {
                return ResponseEntity.ok(Map.of("ok", false, "error", result.error()));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ok", false, "error", "Failed to fetch URL: " + e.getMessage()));
        }
    }
}
