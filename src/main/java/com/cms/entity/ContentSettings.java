package com.cms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"page_name", "section_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_name", nullable = false, length = 100)
    private String pageName;

    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;

    @Column(length = 500)
    private String heading;

    @Column(length = 500)
    private String subheading;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "button_text", length = 255)
    private String buttonText;

    @Column(name = "button_link", length = 500)
    private String buttonLink;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

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
