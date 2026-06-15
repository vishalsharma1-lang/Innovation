package com.cms.controller.api;

import com.cms.service.vehicle.VehicleImportService;
import com.cms.service.vehicle.VehicleImportService.VehicleImportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for importing vehicle data from external URLs.
 * This is additive only — never modifies existing records.
 */
@RestController
@RequestMapping("/api/admin/vehicles/import")
public class VehicleImportApiController {

    @Autowired
    private VehicleImportService importService;

    /**
     * Fetch vehicle data from a single external URL.
     */
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchFromUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            VehicleImportData data = importService.fetchFromUrl(url);
            return ResponseEntity.ok(Map.of(
                "message", "Vehicle data fetched successfully",
                "data", data
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to fetch data from URL: " + e.getMessage(),
                "url", url
            ));
        }
    }

    /**
     * Fetch vehicle data from multiple URLs and merge results.
     * Each URL is fetched independently — data is aggregated.
     */
    @PostMapping("/fetch-multi")
    public ResponseEntity<?> fetchFromMultipleUrls(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> urls = (List<String>) request.get("urls");

        if (urls == null || urls.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "At least one URL is required"));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        VehicleImportData merged = null;

        for (String rawUrl : urls) {
            String url = rawUrl.trim();
            if (url.isBlank()) continue;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            try {
                VehicleImportData data = importService.fetchFromUrl(url);
                results.add(Map.of("url", url, "status", "success", "data", data));

                if (merged == null) {
                    merged = data;
                } else {
                    // Merge data from subsequent URLs into the first
                    mergeData(merged, data);
                }
            } catch (Exception e) {
                results.add(Map.of("url", url, "status", "error", "error", e.getMessage()));
            }
        }

        if (merged == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch data from all URLs", "results", results));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Data fetched from " + results.stream().filter(r -> "success".equals(r.get("status"))).count() + "/" + urls.size() + " URLs",
            "data", merged,
            "results", results
        ));
    }

    /**
     * Merge data from source into target (additive — doesn't overwrite existing non-null fields).
     */
    private void mergeData(VehicleImportData target, VehicleImportData source) {
        // Fill in missing basic fields
        if ((target.name == null || target.name.isBlank()) && source.name != null) target.name = source.name;
        if ((target.brand == null || target.brand.isBlank()) && source.brand != null) target.brand = source.brand;
        if ((target.model == null || target.model.isBlank()) && source.model != null) target.model = source.model;
        if ((target.category == null || target.category.isBlank()) && source.category != null) target.category = source.category;
        if ((target.fuelType == null || target.fuelType.isBlank()) && source.fuelType != null) target.fuelType = source.fuelType;
        if ((target.transmissionType == null || target.transmissionType.isBlank()) && source.transmissionType != null) target.transmissionType = source.transmissionType;
        if (target.seatingCapacity == null && source.seatingCapacity != null) target.seatingCapacity = source.seatingCapacity;
        if (target.startingPrice == null && source.startingPrice != null) target.startingPrice = source.startingPrice;
        if (target.maxPrice == null && source.maxPrice != null) target.maxPrice = source.maxPrice;
        if ((target.shortDescription == null || target.shortDescription.isBlank()) && source.shortDescription != null) target.shortDescription = source.shortDescription;
        if ((target.fullDescription == null || target.fullDescription.isBlank()) && source.fullDescription != null) target.fullDescription = source.fullDescription;
        if ((target.heroImage == null || target.heroImage.isBlank()) && source.heroImage != null) target.heroImage = source.heroImage;
        if ((target.pros == null || target.pros.isBlank()) && source.pros != null) target.pros = source.pros;
        if ((target.cons == null || target.cons.isBlank()) && source.cons != null) target.cons = source.cons;

        // Merge lists (add unique items)
        if (source.specifications != null) {
            if (target.specifications == null) target.specifications = new ArrayList<>();
            Set<String> existingSpecs = new HashSet<>();
            target.specifications.forEach(s -> existingSpecs.add(s.name.toLowerCase()));
            for (var s : source.specifications) {
                if (!existingSpecs.contains(s.name.toLowerCase())) {
                    target.specifications.add(s);
                    existingSpecs.add(s.name.toLowerCase());
                }
            }
        }

        if (source.features != null) {
            if (target.features == null) target.features = new ArrayList<>();
            Set<String> existingFeatures = new HashSet<>();
            target.features.forEach(f -> existingFeatures.add(f.toLowerCase()));
            for (String f : source.features) {
                if (!existingFeatures.contains(f.toLowerCase())) {
                    target.features.add(f);
                    existingFeatures.add(f.toLowerCase());
                }
            }
        }

        if (source.variants != null) {
            if (target.variants == null) target.variants = new ArrayList<>();
            Set<String> existingVariants = new HashSet<>();
            target.variants.forEach(v -> existingVariants.add(v.variantName.toLowerCase()));
            for (var v : source.variants) {
                if (!existingVariants.contains(v.variantName.toLowerCase())) {
                    target.variants.add(v);
                    existingVariants.add(v.variantName.toLowerCase());
                }
            }
        }

        if (source.images != null) {
            if (target.images == null) target.images = new ArrayList<>();
            Set<String> existingUrls = new HashSet<>();
            target.images.forEach(i -> existingUrls.add(i.url));
            for (var i : source.images) {
                if (!existingUrls.contains(i.url) && target.images.size() < 30) {
                    target.images.add(i);
                    existingUrls.add(i.url);
                }
            }
        }

        if (source.colors != null) {
            if (target.colors == null) target.colors = new ArrayList<>();
            Set<String> existingColors = new HashSet<>();
            target.colors.forEach(c -> existingColors.add(c.name.toLowerCase()));
            for (var c : source.colors) {
                if (!existingColors.contains(c.name.toLowerCase())) {
                    target.colors.add(c);
                    existingColors.add(c.name.toLowerCase());
                }
            }
        }

        if (source.faqs != null) {
            if (target.faqs == null) target.faqs = new ArrayList<>();
            Set<String> existingFaqs = new HashSet<>();
            target.faqs.forEach(f -> existingFaqs.add(f.question.toLowerCase()));
            for (var f : source.faqs) {
                if (!existingFaqs.contains(f.question.toLowerCase())) {
                    target.faqs.add(f);
                    existingFaqs.add(f.question.toLowerCase());
                }
            }
        }

        if (source.expertReviews != null) {
            if (target.expertReviews == null) target.expertReviews = new ArrayList<>();
            target.expertReviews.addAll(source.expertReviews);
        }

        if (source.userReviews != null) {
            if (target.userReviews == null) target.userReviews = new ArrayList<>();
            target.userReviews.addAll(source.userReviews);
        }

        if (source.news != null) {
            if (target.news == null) target.news = new ArrayList<>();
            Set<String> existingNews = new HashSet<>();
            target.news.forEach(n -> existingNews.add(n.title.toLowerCase()));
            for (var n : source.news) {
                if (!existingNews.contains(n.title.toLowerCase())) {
                    target.news.add(n);
                    existingNews.add(n.title.toLowerCase());
                }
            }
        }
    }
}
