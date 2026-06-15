package com.cms.controller.api;

import com.cms.entity.SeoSettings;
import com.cms.service.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class SeoApiController {

    @Autowired
    private SeoService seoService;

    /** GET /seo/{page} */
    @GetMapping("/seo/{page}")
    public ResponseEntity<?> getSeoByPage(@PathVariable String page) {
        return seoService.getSeoByPage(page)
                .map(seo -> ResponseEntity.ok((Object) seo))
                .orElse(ResponseEntity.ok(Map.of("message", "No SEO settings found for page: " + page)));
    }

    /** POST /seo */
    @PostMapping("/seo")
    public ResponseEntity<?> createSeo(@RequestBody SeoSettings seoSettings) {
        try {
            SeoSettings saved = seoService.createOrUpdateSeo(seoSettings);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /seo/{id} */
    @PutMapping("/seo/{id}")
    public ResponseEntity<?> updateSeo(@PathVariable Long id, @RequestBody SeoSettings seoSettings) {
        return seoService.getSeoById(id).map(existing -> {
            seoSettings.setId(id);
            SeoSettings updated = seoService.saveSeoSettings(seoSettings);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /seo/{id} */
    @DeleteMapping("/seo/{id}")
    public ResponseEntity<?> deleteSeo(@PathVariable Long id) {
        return seoService.getSeoById(id).map(seo -> {
            seoService.deleteSeoSettings(id);
            return ResponseEntity.ok(Map.of("message", "SEO record deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
