package com.cms.entity.page;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(name = "section_type", nullable = false, length = 50)
    private String sectionType; // hero, heading, text, image, video, button, faq, testimonial, features, pricing, gallery, custom

    @Column(name = "section_key", length = 100)
    private String sectionKey; // unique identifier within page

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    // ─── Content Fields ─────────────────────────────────────

    @Column(length = 500)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String content; // rich text / HTML content

    @Column(name = "plain_text", columnDefinition = "TEXT")
    private String plainText;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_alt", length = 255)
    private String imageAlt;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "button_text", length = 255)
    private String buttonText;

    @Column(name = "button_url", length = 500)
    private String buttonUrl;

    @Column(name = "button_style", length = 50)
    private String buttonStyle; // primary, secondary, outline

    @Column(name = "background_color", length = 20)
    private String backgroundColor;

    @Column(name = "text_color", length = 20)
    private String textColor;

    @Column(name = "css_class", length = 255)
    private String cssClass;

    // JSON data for complex sections (FAQs, features, pricing, testimonials, gallery)
    @Column(name = "json_data", columnDefinition = "TEXT")
    private String jsonData;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
