package com.cms.controller;

import com.cms.entity.Theme;
import com.cms.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class ThemeCssController {

    private final ThemeService service;

    /** Serves the active theme as a CSS file — included on every page */
    @GetMapping(value = "/css/theme.css", produces = "text/css")
    public ResponseEntity<String> themeCss(
            @RequestParam(value = "preview", required = false) Long previewId) {

        Theme theme;
        if (previewId != null) {
            theme = service.findById(previewId).orElse(null);
        } else {
            theme = service.findActive().orElse(null);
        }

        String css = service.buildThemeCss(theme);

        if (previewId != null) {
            // No cache for preview
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/css"))
                .body(css);
        }
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
            .contentType(MediaType.parseMediaType("text/css"))
            .body(css);
    }
}
