package com.cms.controller.api;

import com.cms.service.page.PageReplicateService;
import com.cms.service.page.PageReplicateService.PageExtractResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pages/replicate")
public class PageReplicateApiController {

    @Autowired
    private PageReplicateService replicateService;

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchPage(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }
        if (!url.startsWith("http")) url = "https://" + url;

        try {
            PageExtractResult result = replicateService.extractFromUrl(url);
            return ResponseEntity.ok(Map.of("message", "Page extracted", "data", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }
}
