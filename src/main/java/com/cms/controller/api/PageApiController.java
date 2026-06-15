package com.cms.controller.api;

import com.cms.entity.page.*;
import com.cms.service.page.PageService;
import com.cms.service.page.YouTubeReelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pages")
public class PageApiController {

    @Autowired private PageService pageService;
    @Autowired private YouTubeReelsService youtubeService;

    // ─── Page CRUD ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listPages(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(defaultValue = "") String search) {
        return ResponseEntity.ok(pageService.searchPages(search,
                PageRequest.of(page, size, Sort.by("updatedAt").descending())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPage(@PathVariable Long id) {
        return pageService.getById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPage(@RequestBody DynamicPage pg) {
        if (pg.getTitle() == null || pg.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
        pg.setId(null);
        return ResponseEntity.ok(Map.of("message", "Page created", "data", pageService.savePage(pg)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePage(@PathVariable Long id, @RequestBody DynamicPage pg) {
        return pageService.getById(id).map(existing -> {
            if (pg.getTitle() != null) existing.setTitle(pg.getTitle());
            if (pg.getSlug() != null) existing.setSlug(pg.getSlug());
            if (pg.getTemplateName() != null) existing.setTemplateName(pg.getTemplateName());
            if (pg.getStatus() != null) existing.setStatus(pg.getStatus());
            if (pg.getLanguage() != null) existing.setLanguage(pg.getLanguage());
            if (pg.getSeoTitle() != null) existing.setSeoTitle(pg.getSeoTitle());
            if (pg.getSeoDescription() != null) existing.setSeoDescription(pg.getSeoDescription());
            if (pg.getSeoKeywords() != null) existing.setSeoKeywords(pg.getSeoKeywords());
            if (pg.getOgTitle() != null) existing.setOgTitle(pg.getOgTitle());
            if (pg.getOgDescription() != null) existing.setOgDescription(pg.getOgDescription());
            if (pg.getOgImage() != null) existing.setOgImage(pg.getOgImage());
            if (pg.getCanonicalUrl() != null) existing.setCanonicalUrl(pg.getCanonicalUrl());
            if (pg.getRobots() != null) existing.setRobots(pg.getRobots());
            if (pg.getHeadScripts() != null) existing.setHeadScripts(pg.getHeadScripts());
            if (pg.getBodyScripts() != null) existing.setBodyScripts(pg.getBodyScripts());
            if (pg.getAnalyticsId() != null) existing.setAnalyticsId(pg.getAnalyticsId());
            if (pg.getPublishAt() != null) existing.setPublishAt(pg.getPublishAt());
            if (pg.getIsActive() != null) existing.setIsActive(pg.getIsActive());
            if (pg.getYoutubeEnabled() != null) existing.setYoutubeEnabled(pg.getYoutubeEnabled());
            if (pg.getYoutubeChannelId() != null) existing.setYoutubeChannelId(pg.getYoutubeChannelId());
            if (pg.getYoutubePlaylistId() != null) existing.setYoutubePlaylistId(pg.getYoutubePlaylistId());
            if (pg.getYoutubeLimit() != null) existing.setYoutubeLimit(pg.getYoutubeLimit());
            if (pg.getYoutubeLayout() != null) existing.setYoutubeLayout(pg.getYoutubeLayout());
            if (pg.getYoutubeCacheTime() != null) existing.setYoutubeCacheTime(pg.getYoutubeCacheTime());
            if (pg.getYoutubeAutoplay() != null) existing.setYoutubeAutoplay(pg.getYoutubeAutoplay());
            if (pg.getYoutubeShowTitle() != null) existing.setYoutubeShowTitle(pg.getYoutubeShowTitle());
            if (pg.getYoutubeShowDesc() != null) existing.setYoutubeShowDesc(pg.getYoutubeShowDesc());
            if (pg.getYoutubeInfiniteScroll() != null) existing.setYoutubeInfiniteScroll(pg.getYoutubeInfiniteScroll());
            return ResponseEntity.ok(Map.of("data", pageService.savePage(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePage(@PathVariable Long id) {
        pageService.deletePage(id);
        return ResponseEntity.ok(Map.of("message", "Page deleted"));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishPage(@PathVariable Long id) {
        DynamicPage p = pageService.publishPage(id);
        return p != null ? ResponseEntity.ok(Map.of("message", "Published", "data", p))
                         : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublishPage(@PathVariable Long id) {
        DynamicPage p = pageService.unpublishPage(id);
        return p != null ? ResponseEntity.ok(Map.of("message", "Unpublished", "data", p))
                         : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<?> clonePage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        DynamicPage cloned = pageService.clonePage(id, body.get("title"), body.get("slug"));
        if (cloned == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("message", "Page cloned", "data", cloned));
    }

    // ─── Sections CRUD ─────────────────────────────────────

    @GetMapping("/{pageId}/sections")
    public ResponseEntity<?> getSections(@PathVariable Long pageId) {
        return ResponseEntity.ok(pageService.getSections(pageId));
    }

    @PostMapping("/{pageId}/sections")
    public ResponseEntity<?> createSection(@PathVariable Long pageId, @RequestBody PageSection section) {
        section.setId(null);
        section.setPageId(pageId);
        return ResponseEntity.ok(Map.of("data", pageService.saveSection(section)));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<?> updateSection(@PathVariable Long id, @RequestBody PageSection section) {
        return pageService.getSectionById(id).map(existing -> {
            if (section.getSectionType() != null) existing.setSectionType(section.getSectionType());
            if (section.getSectionKey() != null) existing.setSectionKey(section.getSectionKey());
            if (section.getDisplayOrder() != null) existing.setDisplayOrder(section.getDisplayOrder());
            if (section.getIsVisible() != null) existing.setIsVisible(section.getIsVisible());
            if (section.getTitle() != null) existing.setTitle(section.getTitle());
            if (section.getSubtitle() != null) existing.setSubtitle(section.getSubtitle());
            if (section.getContent() != null) existing.setContent(section.getContent());
            if (section.getPlainText() != null) existing.setPlainText(section.getPlainText());
            if (section.getImageUrl() != null) existing.setImageUrl(section.getImageUrl());
            if (section.getImageAlt() != null) existing.setImageAlt(section.getImageAlt());
            if (section.getVideoUrl() != null) existing.setVideoUrl(section.getVideoUrl());
            if (section.getButtonText() != null) existing.setButtonText(section.getButtonText());
            if (section.getButtonUrl() != null) existing.setButtonUrl(section.getButtonUrl());
            if (section.getButtonStyle() != null) existing.setButtonStyle(section.getButtonStyle());
            if (section.getBackgroundColor() != null) existing.setBackgroundColor(section.getBackgroundColor());
            if (section.getTextColor() != null) existing.setTextColor(section.getTextColor());
            if (section.getCssClass() != null) existing.setCssClass(section.getCssClass());
            if (section.getJsonData() != null) existing.setJsonData(section.getJsonData());
            return ResponseEntity.ok(Map.of("data", pageService.saveSection(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<?> deleteSection(@PathVariable Long id) {
        pageService.deleteSection(id);
        return ResponseEntity.ok(Map.of("message", "Section deleted"));
    }

    @PostMapping("/sections/{id}/toggle")
    public ResponseEntity<?> toggleSection(@PathVariable Long id) {
        pageService.toggleSectionVisibility(id);
        return ResponseEntity.ok(Map.of("message", "Visibility toggled"));
    }

    @PostMapping("/{pageId}/sections/reorder")
    public ResponseEntity<?> reorderSections(@PathVariable Long pageId, @RequestBody List<Long> sectionIds) {
        pageService.reorderSections(pageId, sectionIds);
        return ResponseEntity.ok(Map.of("message", "Reordered"));
    }

    // ─── Versioning ───────────────────────────────────────

    @GetMapping("/{pageId}/versions")
    public ResponseEntity<?> getVersions(@PathVariable Long pageId) {
        return ResponseEntity.ok(pageService.getVersionHistory(pageId));
    }

    @PostMapping("/{pageId}/versions")
    public ResponseEntity<?> createVersion(@PathVariable Long pageId, @RequestBody Map<String, String> body) {
        PageVersion v = pageService.createVersion(pageId, body.get("note"), body.get("user"));
        if (v == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("message", "Version saved", "data", v));
    }

    @PostMapping("/{pageId}/rollback/{versionId}")
    public ResponseEntity<?> rollback(@PathVariable Long pageId, @PathVariable Long versionId) {
        boolean ok = pageService.rollbackToVersion(pageId, versionId);
        return ok ? ResponseEntity.ok(Map.of("message", "Rolled back"))
                  : ResponseEntity.badRequest().body(Map.of("error", "Rollback failed"));
    }

    // ─── YouTube Sync ─────────────────────────────────────

    @PostMapping("/{id}/youtube/sync")
    public ResponseEntity<?> syncYouTube(@PathVariable Long id) {
        return pageService.getById(id).map(page -> {
            youtubeService.syncVideos(page);
            return ResponseEntity.ok(Map.of("message", "YouTube cache refreshed"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/youtube/preview")
    public ResponseEntity<?> previewYouTube(@PathVariable Long id) {
        return pageService.getById(id).map(page -> {
            var videos = youtubeService.getVideosForPage(page);
            return ResponseEntity.ok(Map.of("videos", videos, "count", videos.size()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
