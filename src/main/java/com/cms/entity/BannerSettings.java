package com.cms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "banner_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_name", nullable = false, length = 100)
    private String pageName;

    @Column(name = "banner_title", length = 500)
    private String bannerTitle;

    @Column(name = "banner_subtitle", length = 500)
    private String bannerSubtitle;

    @Column(name = "banner_image", length = 500)
    private String bannerImage;

    @Column(name = "cta_button_text", length = 255)
    private String ctaButtonText;

    @Column(name = "cta_button_link", length = 500)
    private String ctaButtonLink;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
