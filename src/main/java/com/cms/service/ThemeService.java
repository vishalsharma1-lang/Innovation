package com.cms.service;

import com.cms.entity.Theme;
import com.cms.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository repo;

    public List<Theme> findAll() {
        return repo.findAll();
    }

    public Optional<Theme> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<Theme> findActive() {
        return repo.findByIsActiveTrue();
    }

    @Transactional
    public Theme save(Theme theme) {
        return repo.save(theme);
    }

    @Transactional
    public Theme activate(Long id) {
        repo.deactivateAll();
        Theme theme = repo.findById(id).orElseThrow(() -> new RuntimeException("Theme not found"));
        theme.setActive(true);
        return repo.save(theme);
    }

    @Transactional
    public void delete(Long id) {
        Theme theme = repo.findById(id).orElseThrow(() -> new RuntimeException("Theme not found"));
        if (theme.isActive()) throw new RuntimeException("Cannot delete the active theme");
        repo.deleteById(id);
    }

    /** Build the full CSS :root block from the active theme */
    public String buildThemeCss(Theme t) {
        if (t == null) return "";
        StringBuilder sb = new StringBuilder();

        // Google Fonts import
        String fonts = buildFontImport(t.getFontHeading(), t.getFontBody());
        if (!fonts.isEmpty()) sb.append(fonts).append("\n\n");

        sb.append(":root {\n");
        sb.append("  --primary: ").append(t.getColorPrimary()).append(";\n");
        sb.append("  --primary-dark: ").append(t.getColorPrimaryDark()).append(";\n");
        sb.append("  --primary-light: ").append(t.getColorPrimaryLight()).append(";\n");
        sb.append("  --secondary: ").append(t.getColorSecondary()).append(";\n");
        sb.append("  --dark: ").append(t.getColorDark()).append(";\n");
        sb.append("  --text: ").append(t.getColorText()).append(";\n");
        sb.append("  --text-light: ").append(t.getColorTextLight()).append(";\n");
        sb.append("  --text-muted: ").append(t.getColorTextMuted()).append(";\n");
        sb.append("  --bg-light: ").append(t.getColorBgLight()).append(";\n");
        sb.append("  --bg-section: ").append(t.getColorBgLight()).append(";\n");
        sb.append("  --border: ").append(t.getColorBorder()).append(";\n");
        sb.append("  --radius: ").append(t.getBorderRadius()).append(";\n");
        sb.append("  --radius-sm: ").append(t.getBorderRadiusSm()).append(";\n");
        sb.append("  --accent: ").append(t.getColorAccent()).append(";\n");
        sb.append("  --green: ").append(t.getColorSuccess()).append(";\n");
        sb.append("  --font-heading: '").append(safe(t.getFontHeading())).append("';\n");
        sb.append("  --font-body: '").append(safe(t.getFontBody())).append("';\n");
        sb.append("  --font-size-base: ").append(t.getFontSizeBase()).append(";\n");
        sb.append("  --btn-radius: ").append(t.getBtnRadius()).append(";\n");
        sb.append("  --hero-bg: ").append(t.getHeroBg()).append(";\n");
        sb.append("  --hero-text: ").append(t.getHeroTextColor()).append(";\n");
        sb.append("}\n\n");

        // Body / font overrides
        sb.append("body { font-family: var(--font-body), 'Inter', sans-serif; font-size: var(--font-size-base); }\n");
        sb.append("h1,h2,h3,h4,h5,h6,.navbar-brand,.vehicle-card-title { font-family: var(--font-heading), 'Outfit', sans-serif; }\n\n");

        // Top bar
        sb.append(".top-bar { background: ").append(t.getTopBarBg()).append(" !important; }\n");
        sb.append(".top-bar, .top-bar a, .top-bar-item { color: ").append(t.getTopBarTextColor()).append(" !important; }\n\n");

        // Navbar
        sb.append(".navbar.nav-solid, .navbar.scrolled { background: ").append(t.getNavSolidBg()).append(" !important; }\n\n");

        // Buttons
        sb.append(".btn-primary-gradient, .btn-nav-cta {\n");
        sb.append("  background: ").append(t.getBtnPrimaryBg()).append(" !important;\n");
        sb.append("  color: ").append(t.getBtnPrimaryText()).append(" !important;\n");
        sb.append("  border-color: ").append(t.getBtnPrimaryBorder()).append(" !important;\n");
        sb.append("  border-radius: var(--btn-radius) !important;\n");
        sb.append("}\n\n");

        // Footer
        sb.append(".site-footer { background: ").append(t.getFooterBg()).append(" !important; }\n");
        sb.append(".site-footer, .footer-desc { color: ").append(t.getFooterTextColor()).append(" !important; }\n");
        sb.append(".footer-heading { color: ").append(t.getFooterHeadingColor()).append(" !important; }\n");
        sb.append(".footer-links a { color: ").append(t.getFooterLinkColor()).append(" !important; }\n\n");

        // Hero section
        sb.append(".hero-section, .hero-bg { background: var(--hero-bg) !important; color: var(--hero-text) !important; }\n");

        return sb.toString();
    }

    private String buildFontImport(String heading, String body) {
        if (heading == null && body == null) return "";
        StringBuilder sb = new StringBuilder();
        java.util.Set<String> fonts = new java.util.LinkedHashSet<>();
        if (heading != null && !heading.isBlank()) fonts.add(heading.trim());
        if (body != null && !body.isBlank() && !body.equals(heading)) fonts.add(body.trim());
        if (fonts.isEmpty()) return "";
        String params = fonts.stream()
            .map(f -> "family=" + f.replace(" ", "+") + ":wght@300;400;500;600;700;800;900")
            .collect(java.util.stream.Collectors.joining("&"));
        sb.append("@import url('https://fonts.googleapis.com/css2?").append(params).append("&display=swap');");
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("'", "\\'");
    }
}
