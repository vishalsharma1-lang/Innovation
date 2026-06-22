package com.cms.config;

import com.cms.entity.Theme;
import com.cms.repository.ThemeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ThemeSeeder {

    private final ThemeRepository repo;

    @PostConstruct
    @Transactional
    public void seed() {
        if (repo.count() > 0) return;

        // ── Dark Theme (Active by default) ──────────────────────
        Theme dark = new Theme();
        dark.setName("Dark Navy");
        dark.setDescription("Deep dark blue theme with orange accents — default look");
        dark.setActive(true);
        dark.setDefault(true);

        dark.setColorPrimary("#FF6B00");
        dark.setColorPrimaryDark("#E05E00");
        dark.setColorPrimaryLight("#FFF3E8");
        dark.setColorSecondary("#1E3A5F");
        dark.setColorDark("#0F172A");
        dark.setColorAccent("#3B82F6");
        dark.setColorSuccess("#10B981");
        dark.setColorText("#1E293B");
        dark.setColorTextLight("#475569");
        dark.setColorTextMuted("#94A3B8");
        dark.setColorBgLight("#F8FAFC");
        dark.setColorBorder("#E2E8F0");

        dark.setFontHeading("Outfit");
        dark.setFontBody("Inter");
        dark.setFontSizeBase("15px");
        dark.setBorderRadius("14px");
        dark.setBorderRadiusSm("8px");

        dark.setTopBarBg("linear-gradient(90deg,#0f172a 0%,#1e3a5f 50%,#0f172a 100%)");
        dark.setTopBarTextColor("rgba(255,255,255,0.65)");
        dark.setHeaderBg("linear-gradient(135deg,#0f172a 0%,#1e3a5f 100%)");
        dark.setHeaderTextColor("#ffffff");
        dark.setNavSolidBg("#0f172a");

        dark.setHeroBg("linear-gradient(135deg,#0f172a 0%,#1e3a5f 60%,#0f172a 100%)");
        dark.setHeroTextColor("#ffffff");

        dark.setFooterBg("linear-gradient(135deg,#0f172a 0%,#1e3a5f 100%)");
        dark.setFooterTextColor("rgba(255,255,255,0.6)");
        dark.setFooterHeadingColor("#ffffff");
        dark.setFooterLinkColor("rgba(255,255,255,0.55)");

        dark.setBtnRadius("50px");
        dark.setBtnPrimaryBg("linear-gradient(135deg,#FF6B00,#FF8C38)");
        dark.setBtnPrimaryText("#ffffff");
        dark.setBtnPrimaryBorder("transparent");

        dark.setBrandName("CarDekho Deals");
        dark.setBrandTagline("India's Best Car Deals");

        repo.save(dark);

        // ── Light / Clean Theme ──────────────────────────────────
        Theme light = new Theme();
        light.setName("Clean White");
        light.setDescription("Minimal white theme with blue primary — modern and fresh");
        light.setActive(false);
        light.setDefault(false);

        light.setColorPrimary("#2563EB");
        light.setColorPrimaryDark("#1D4ED8");
        light.setColorPrimaryLight("#EFF6FF");
        light.setColorSecondary("#374151");
        light.setColorDark("#111827");
        light.setColorAccent("#7C3AED");
        light.setColorSuccess("#059669");
        light.setColorText("#111827");
        light.setColorTextLight("#374151");
        light.setColorTextMuted("#6B7280");
        light.setColorBgLight("#F9FAFB");
        light.setColorBorder("#E5E7EB");

        light.setFontHeading("Poppins");
        light.setFontBody("Inter");
        light.setFontSizeBase("15px");
        light.setBorderRadius("12px");
        light.setBorderRadiusSm("6px");

        light.setTopBarBg("#111827");
        light.setTopBarTextColor("rgba(255,255,255,0.7)");
        light.setHeaderBg("#ffffff");
        light.setHeaderTextColor("#111827");
        light.setNavSolidBg("#ffffff");

        light.setHeroBg("linear-gradient(135deg,#1e3a8a 0%,#2563eb 100%)");
        light.setHeroTextColor("#ffffff");

        light.setFooterBg("#111827");
        light.setFooterTextColor("rgba(255,255,255,0.6)");
        light.setFooterHeadingColor("#ffffff");
        light.setFooterLinkColor("rgba(255,255,255,0.55)");

        light.setBtnRadius("8px");
        light.setBtnPrimaryBg("linear-gradient(135deg,#2563EB,#3B82F6)");
        light.setBtnPrimaryText("#ffffff");
        light.setBtnPrimaryBorder("transparent");

        light.setBrandName("CarDekho Deals");
        light.setBrandTagline("India's Best Car Deals");

        repo.save(light);
    }
}
