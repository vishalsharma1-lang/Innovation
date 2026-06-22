package com.cms.controller.api;

import com.cms.entity.Theme;
import com.cms.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeApiController {

    private final ThemeService service;

    @GetMapping
    public List<Theme> list() {
        return service.findAll();
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        return service.findActive()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.activate(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
