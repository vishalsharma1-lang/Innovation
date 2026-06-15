package com.cms.controller.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/seo")
public class SeoFetchController {

    @PostMapping("/fetch-url")
    public ResponseEntity<?> fetchSeoFromUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }
        if (!url.startsWith("http")) url = "https://" + url;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            Map<String, String> result = new HashMap<>();

            // Title
            result.put("seoTitle", doc.title());

            // Meta description
            Element metaDesc = doc.selectFirst("meta[name=description]");
            if (metaDesc != null) result.put("metaDescription", metaDesc.attr("content"));

            // Keywords
            Element metaKeys = doc.selectFirst("meta[name=keywords]");
            if (metaKeys != null) result.put("keywords", metaKeys.attr("content"));

            // Canonical
            Element canonical = doc.selectFirst("link[rel=canonical]");
            if (canonical != null) result.put("canonicalUrl", canonical.attr("href"));

            // Robots
            Element robots = doc.selectFirst("meta[name=robots]");
            if (robots != null) result.put("robots", robots.attr("content"));

            // OG Title
            Element ogTitle = doc.selectFirst("meta[property=og:title]");
            if (ogTitle != null) result.put("ogTitle", ogTitle.attr("content"));

            // OG Description
            Element ogDesc = doc.selectFirst("meta[property=og:description]");
            if (ogDesc != null) result.put("ogDescription", ogDesc.attr("content"));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch: " + e.getMessage()));
        }
    }
}
