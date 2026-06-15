package com.cms.controller.api;

import com.cms.service.analytics.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsApiController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/event")
    public ResponseEntity<?> trackEvent(@RequestBody Map<String, Object> payload,
                                         HttpServletRequest request) {
        try {
            analyticsService.trackEvent(payload, request);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
