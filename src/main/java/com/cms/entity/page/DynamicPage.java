package com.cms.entity.page;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "dynamic_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "template_name", length = 100)
    private String templateName; // which template to use (vehicle-detail, landing, etc.)

    @Column(name = "source_page_id")
    private Long sourcePageId; // cloned from which page

    @Column(length = 50)
    private String status = "draft"; // draft, published, scheduled

    @Column(length = 20)
    private String language = "en";

    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    @Column(name = "version_number")
    private Integer versionNumber = 1;

    // SEO
    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", columnDefinition = "TEXT")
    private String seoDescription;

    @Column(name = "seo_keywords", columnDefinition = "TEXT")
    private String seoKeywords;

    @Column(name = "og_title", length = 255)
    private String ogTitle;

    @Column(name = "og_description", columnDefinition = "TEXT")
    private String ogDescription;

    @Column(name = "og_image", length = 500)
    private String ogImage;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(length = 100)
    private String robots = "index, follow";

    // Custom scripts
    @Column(name = "head_scripts", columnDefinition = "TEXT")
    private String headScripts;

    @Column(name = "body_scripts", columnDefinition = "TEXT")
    private String bodyScripts;

    // Analytics
    @Column(name = "analytics_id", length = 100)
    private String analyticsId;

    // YouTube Reels Settings
    @Column(name = "youtube_enabled")
    private Boolean youtubeEnabled = false;

    @Column(name = "youtube_channel_id", length = 100)
    private String youtubeChannelId;

    @Column(name = "youtube_playlist_id", length = 100)
    private String youtubePlaylistId;

    @Column(name = "youtube_limit")
    private Integer youtubeLimit = 6;

    @Column(name = "youtube_layout", length = 30)
    private String youtubeLayout = "grid"; // grid, carousel, slider, shorts

    @Column(name = "youtube_cache_time")
    private Integer youtubeCacheTime = 60; // minutes

    @Column(name = "youtube_autoplay")
    private Boolean youtubeAutoplay = false;

    @Column(name = "youtube_show_title")
    private Boolean youtubeShowTitle = true;

    @Column(name = "youtube_show_desc")
    private Boolean youtubeShowDesc = false;

    @Column(name = "youtube_infinite_scroll")
    private Boolean youtubeInfiniteScroll = false;

    @Column(name = "youtube_cache_data", columnDefinition = "TEXT")
    private String youtubeCacheData; // cached JSON response

    @Column(name = "youtube_cache_updated")
    private LocalDateTime youtubeCacheUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
