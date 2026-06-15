package com.cms.controller.api;

import com.cms.entity.*;
import com.cms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API for admin panel AJAX operations.
 * Provides real-time CRUD without full page refresh.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired private SeoService seoService;
    @Autowired private ContentService contentService;
    @Autowired private BannerService bannerService;
    @Autowired private ImageService imageService;

    // ─── SEO CRUD ──────────────────────────────────────────

    @GetMapping("/seo")
    public ResponseEntity<?> listSeo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        Page<SeoSettings> result = seoService.searchSeoSettings(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seo/{id}")
    public ResponseEntity<?> getSeo(@PathVariable Long id) {
        return seoService.getSeoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/seo")
    public ResponseEntity<?> createSeo(@RequestBody SeoSettings seo) {
        if (seo.getPageName() == null || seo.getPageName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Page name is required"));
        }
        seo.setId(null); // Force new record
        seo.setIsDeleted(false);
        SeoSettings saved = seoService.saveSeoSettings(seo);
        return ResponseEntity.ok(Map.of("message", "SEO settings created", "data", saved));
    }

    @PutMapping("/seo/{id}")
    public ResponseEntity<?> updateSeo(@PathVariable Long id, @RequestBody SeoSettings seo) {
        return seoService.getSeoById(id).map(existing -> {
            existing.setSeoTitle(seo.getSeoTitle());
            existing.setMetaDescription(seo.getMetaDescription());
            existing.setKeywords(seo.getKeywords());
            existing.setCanonicalUrl(seo.getCanonicalUrl());
            existing.setRobots(seo.getRobots());
            existing.setOgTitle(seo.getOgTitle());
            existing.setOgDescription(seo.getOgDescription());
            if (seo.getPageName() != null) existing.setPageName(seo.getPageName());
            SeoSettings saved = seoService.saveSeoSettings(existing);
            return ResponseEntity.ok(Map.of("message", "SEO settings updated", "data", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/seo/{id}")
    public ResponseEntity<?> deleteSeo(@PathVariable Long id) {
        return seoService.getSeoById(id).map(existing -> {
            seoService.deleteSeoSettings(id);
            return ResponseEntity.ok(Map.of("message", "SEO record deleted (soft)"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── CONTENT CRUD ──────────────────────────────────────

    @GetMapping("/content")
    public ResponseEntity<?> listContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        Page<ContentSettings> result = contentService.searchContent(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/content/{id}")
    public ResponseEntity<?> getContent(@PathVariable Long id) {
        return contentService.getContentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/content")
    public ResponseEntity<?> createContent(@RequestBody ContentSettings content) {
        if (content.getPageName() == null || content.getPageName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Page name is required"));
        }
        if (content.getSectionName() == null || content.getSectionName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Section name is required"));
        }
        content.setId(null); // Force new record
        content.setIsDeleted(false);
        if (content.getIsActive() == null) content.setIsActive(true);
        ContentSettings saved = contentService.saveContent(content);
        return ResponseEntity.ok(Map.of("message", "Content created", "data", saved));
    }

    @PutMapping("/content/{id}")
    public ResponseEntity<?> updateContent(@PathVariable Long id, @RequestBody ContentSettings content) {
        return contentService.getContentById(id).map(existing -> {
            if (content.getPageName() != null) existing.setPageName(content.getPageName());
            if (content.getSectionName() != null) existing.setSectionName(content.getSectionName());
            existing.setHeading(content.getHeading());
            existing.setSubheading(content.getSubheading());
            existing.setDescription(content.getDescription());
            existing.setButtonText(content.getButtonText());
            existing.setButtonLink(content.getButtonLink());
            existing.setImageUrl(content.getImageUrl());
            if (content.getIsActive() != null) existing.setIsActive(content.getIsActive());
            ContentSettings saved = contentService.saveContent(existing);
            return ResponseEntity.ok(Map.of("message", "Content updated", "data", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        return contentService.getContentById(id).map(existing -> {
            contentService.deleteContent(id);
            return ResponseEntity.ok(Map.of("message", "Content deleted (soft)"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── BANNER CRUD ──────────────────────────────────────

    @GetMapping("/banners")
    public ResponseEntity<?> listBanners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        Page<BannerSettings> result = bannerService.searchBanners(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/banners/{id}")
    public ResponseEntity<?> getBanner(@PathVariable Long id) {
        return bannerService.getBannerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/banners")
    public ResponseEntity<?> createBanner(@RequestBody BannerSettings banner) {
        if (banner.getPageName() == null || banner.getPageName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Page name is required"));
        }
        banner.setId(null); // Force new record
        banner.setIsDeleted(false);
        if (banner.getIsActive() == null) banner.setIsActive(true);
        if (banner.getDisplayOrder() == null) banner.setDisplayOrder(0);
        BannerSettings saved = bannerService.saveBanner(banner);
        return ResponseEntity.ok(Map.of("message", "Banner created", "data", saved));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody BannerSettings banner) {
        return bannerService.getBannerById(id).map(existing -> {
            if (banner.getPageName() != null) existing.setPageName(banner.getPageName());
            existing.setBannerTitle(banner.getBannerTitle());
            existing.setBannerSubtitle(banner.getBannerSubtitle());
            existing.setBannerImage(banner.getBannerImage());
            existing.setCtaButtonText(banner.getCtaButtonText());
            existing.setCtaButtonLink(banner.getCtaButtonLink());
            if (banner.getDisplayOrder() != null) existing.setDisplayOrder(banner.getDisplayOrder());
            if (banner.getIsActive() != null) existing.setIsActive(banner.getIsActive());
            BannerSettings saved = bannerService.saveBanner(existing);
            return ResponseEntity.ok(Map.of("message", "Banner updated", "data", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        return bannerService.getBannerById(id).map(existing -> {
            bannerService.deleteBanner(id);
            return ResponseEntity.ok(Map.of("message", "Banner deleted (soft)"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── IMAGE LIST ──────────────────────────────────────

    @GetMapping("/images")
    public ResponseEntity<?> listImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String search) {
        Page<ImageSettings> result = imageService.searchImages(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        try {
            imageService.deleteImage(id);
            return ResponseEntity.ok(Map.of("message", "Image deleted (soft)"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── DASHBOARD STATS ──────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<?> dashboardStats() {
        return ResponseEntity.ok(Map.of(
            "totalSeo", seoService.searchSeoSettings("", PageRequest.of(0, 1)).getTotalElements(),
            "totalContent", contentService.searchContent("", PageRequest.of(0, 1)).getTotalElements(),
            "totalImages", imageService.searchImages("", PageRequest.of(0, 1)).getTotalElements(),
            "totalBanners", bannerService.searchBanners("", PageRequest.of(0, 1)).getTotalElements()
        ));
    }
}
