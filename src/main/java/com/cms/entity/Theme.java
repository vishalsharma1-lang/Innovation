package com.cms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "themes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    // ── Colors ──────────────────────────────────────────────
    @Column(length = 30)
    private String colorPrimary = "#FF5A00";

    @Column(length = 30)
    private String colorPrimaryDark = "#D44700";

    @Column(length = 30)
    private String colorPrimaryLight = "#FFF5F0";

    @Column(length = 30)
    private String colorSecondary = "#1F2937";

    @Column(length = 30)
    private String colorDark = "#0F172A";

    @Column(length = 30)
    private String colorText = "#1E293B";

    @Column(length = 30)
    private String colorTextLight = "#475569";

    @Column(length = 30)
    private String colorTextMuted = "#94A3B8";

    @Column(length = 30)
    private String colorBgLight = "#F8FAFC";

    @Column(length = 30)
    private String colorBorder = "#E2E8F0";

    @Column(length = 30)
    private String colorAccent = "#3B82F6";

    @Column(length = 30)
    private String colorSuccess = "#10B981";

    // ── Typography ───────────────────────────────────────────
    @Column(length = 100)
    private String fontHeading = "Outfit";

    @Column(length = 100)
    private String fontBody = "Inter";

    @Column(length = 10)
    private String fontSizeBase = "15px";

    // ── Header / Nav ─────────────────────────────────────────
    @Column(length = 200)
    private String headerBg = "linear-gradient(135deg,#0f172a 0%,#1e3a5f 100%)";

    @Column(length = 30)
    private String headerTextColor = "#ffffff";

    @Column(length = 200)
    private String topBarBg = "linear-gradient(90deg,#0f172a 0%,#1e3a5f 50%,#0f172a 100%)";

    @Column(length = 30)
    private String topBarTextColor = "rgba(255,255,255,0.7)";

    @Column(length = 30)
    private String navSolidBg = "#0f172a";

    // ── Footer ───────────────────────────────────────────────
    @Column(length = 200)
    private String footerBg = "linear-gradient(135deg,#0f172a 0%,#1e3a5f 100%)";

    @Column(length = 30)
    private String footerTextColor = "rgba(255,255,255,0.7)";

    @Column(length = 30)
    private String footerHeadingColor = "#ffffff";

    @Column(length = 30)
    private String footerLinkColor = "rgba(255,255,255,0.55)";

    // ── Buttons ──────────────────────────────────────────────
    @Column(length = 30)
    private String btnRadius = "50px";

    @Column(length = 200)
    private String btnPrimaryBg = "linear-gradient(135deg,#FF5A00,#FF8C38)";

    @Column(length = 30)
    private String btnPrimaryText = "#ffffff";

    @Column(length = 30)
    private String btnPrimaryBorder = "transparent";

    // ── Cards / UI ───────────────────────────────────────────
    @Column(length = 30)
    private String borderRadius = "14px";

    @Column(length = 30)
    private String borderRadiusSm = "8px";

    // ── Branding ─────────────────────────────────────────────
    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String faviconUrl;

    @Column(length = 100)
    private String brandName = "CarDekho Deals";

    @Column(length = 150)
    private String brandTagline = "India's Best Car Deals";

    // ── Hero Section ─────────────────────────────────────────
    @Column(length = 200)
    private String heroBg = "linear-gradient(135deg,#0f172a 0%,#1e3a5f 60%,#0f172a 100%)";

    @Column(length = 30)
    private String heroTextColor = "#ffffff";

    // ── Status ───────────────────────────────────────────────
    @Column(nullable = false)
    private boolean isActive = false;

    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
