package com.cms.controller.api;

import com.cms.entity.auto.*;
import com.cms.entity.vehicle.*;
import com.cms.repository.auto.*;
import com.cms.service.vehicle.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Automobile-specific features API.
 * Car Comparison, EMI Calculator, Lead Capture, Dealers, Blog.
 */
@RestController
@RequestMapping("/api/auto")
public class AutoFeaturesApiController {

    @Autowired private CarComparisonRepository comparisonRepo;
    @Autowired private LeadCaptureRepository leadRepo;
    @Autowired private DealerRepository dealerRepo;
    @Autowired private BlogPostRepository blogRepo;
    @Autowired private VehicleService vehicleService;

    // ═══════════════════════════════════════════════════════
    // CAR COMPARISON
    // ═══════════════════════════════════════════════════════

    @GetMapping("/compare/{id1}/{id2}")
    public ResponseEntity<?> compareTwoCars(@PathVariable Long id1, @PathVariable Long id2) {
        var v1 = vehicleService.getFullVehicleData(id1);
        var v2 = vehicleService.getFullVehicleData(id2);
        if (v1 == null || v2 == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("vehicle1", v1, "vehicle2", v2));
    }

    @GetMapping("/compare/{id1}/{id2}/{id3}")
    public ResponseEntity<?> compareThreeCars(@PathVariable Long id1, @PathVariable Long id2, @PathVariable Long id3) {
        var v1 = vehicleService.getFullVehicleData(id1);
        var v2 = vehicleService.getFullVehicleData(id2);
        var v3 = vehicleService.getFullVehicleData(id3);
        if (v1 == null || v2 == null) return ResponseEntity.notFound().build();
        Map<String, Object> result = new HashMap<>();
        result.put("vehicle1", v1);
        result.put("vehicle2", v2);
        if (v3 != null) result.put("vehicle3", v3);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/comparisons")
    public ResponseEntity<?> listComparisons(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(comparisonRepo.findByIsDeletedFalse(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/comparisons")
    public ResponseEntity<?> createComparison(@RequestBody CarComparison c) {
        c.setId(null);
        c.setIsDeleted(false);
        if (c.getSlug() == null || c.getSlug().isBlank()) {
            c.setSlug("compare-" + c.getVehicleId1() + "-vs-" + c.getVehicleId2());
        }
        return ResponseEntity.ok(Map.of("data", comparisonRepo.save(c)));
    }

    @DeleteMapping("/comparisons/{id}")
    public ResponseEntity<?> deleteComparison(@PathVariable Long id) {
        comparisonRepo.findById(id).ifPresent(c -> { c.setIsDeleted(true); comparisonRepo.save(c); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ═══════════════════════════════════════════════════════
    // EMI CALCULATOR
    // ═══════════════════════════════════════════════════════

    @PostMapping("/emi/calculate")
    public ResponseEntity<?> calculateEmi(@RequestBody Map<String, Object> request) {
        double principal = Double.parseDouble(request.getOrDefault("loanAmount", "0").toString());
        double rate = Double.parseDouble(request.getOrDefault("interestRate", "8.5").toString());
        int tenure = Integer.parseInt(request.getOrDefault("tenureMonths", "60").toString());
        double downPayment = Double.parseDouble(request.getOrDefault("downPayment", "0").toString());

        double loanAmount = principal - downPayment;
        double monthlyRate = rate / 12 / 100;

        double emi;
        if (monthlyRate == 0) {
            emi = loanAmount / tenure;
        } else {
            emi = (loanAmount * monthlyRate * Math.pow(1 + monthlyRate, tenure))
                    / (Math.pow(1 + monthlyRate, tenure) - 1);
        }

        double totalPayment = emi * tenure;
        double totalInterest = totalPayment - loanAmount;

        return ResponseEntity.ok(Map.of(
            "emi", Math.round(emi),
            "loanAmount", Math.round(loanAmount),
            "totalPayment", Math.round(totalPayment),
            "totalInterest", Math.round(totalInterest),
            "downPayment", Math.round(downPayment),
            "tenureMonths", tenure,
            "interestRate", rate
        ));
    }

    // ═══════════════════════════════════════════════════════
    // LEAD CAPTURE
    // ═══════════════════════════════════════════════════════

    @GetMapping("/leads")
    public ResponseEntity<?> listLeads(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "") String search,
                                        @RequestParam(defaultValue = "") String status) {
        if (!search.isBlank()) {
            return ResponseEntity.ok(leadRepo.searchLeads(search, PageRequest.of(page, size, Sort.by("createdAt").descending())));
        }
        if (!status.isBlank()) {
            return ResponseEntity.ok(leadRepo.findByStatusAndIsDeletedFalse(status, PageRequest.of(page, size, Sort.by("createdAt").descending())));
        }
        return ResponseEntity.ok(leadRepo.findByIsDeletedFalse(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/leads")
    public ResponseEntity<?> captureLead(@RequestBody LeadCapture lead) {
        if (lead.getCustomerName() == null || lead.getPhone() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name and phone are required"));
        }
        lead.setId(null);
        lead.setStatus("new");
        lead.setIsDeleted(false);
        return ResponseEntity.ok(Map.of("message", "Lead captured", "data", leadRepo.save(lead)));
    }

    @PutMapping("/leads/{id}/status")
    public ResponseEntity<?> updateLeadStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return leadRepo.findById(id).map(lead -> {
            if (body.get("status") != null) lead.setStatus(body.get("status"));
            if (body.get("assignedTo") != null) lead.setAssignedTo(body.get("assignedTo"));
            return ResponseEntity.ok(Map.of("data", leadRepo.save(lead)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/leads/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        leadRepo.findById(id).ifPresent(l -> { l.setIsDeleted(true); leadRepo.save(l); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ═══════════════════════════════════════════════════════
    // DEALER MANAGEMENT
    // ═══════════════════════════════════════════════════════

    @GetMapping("/dealers")
    public ResponseEntity<?> listDealers(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(dealerRepo.findByIsDeletedFalse(PageRequest.of(page, size, Sort.by("dealerName").ascending())));
    }

    @GetMapping("/dealers/city/{city}")
    public ResponseEntity<?> dealersByCity(@PathVariable String city) {
        return ResponseEntity.ok(dealerRepo.findByCityIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(city));
    }

    @GetMapping("/dealers/brand/{brand}")
    public ResponseEntity<?> dealersByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(dealerRepo.findByBrandIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(brand));
    }

    @GetMapping("/dealers/{brand}/{city}")
    public ResponseEntity<?> dealersByBrandCity(@PathVariable String brand, @PathVariable String city) {
        return ResponseEntity.ok(dealerRepo.findByBrandIgnoreCaseAndCityIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(brand, city));
    }

    @PostMapping("/dealers")
    public ResponseEntity<?> createDealer(@RequestBody Dealer dealer) {
        dealer.setId(null);
        dealer.setIsDeleted(false);
        return ResponseEntity.ok(Map.of("data", dealerRepo.save(dealer)));
    }

    @PutMapping("/dealers/{id}")
    public ResponseEntity<?> updateDealer(@PathVariable Long id, @RequestBody Dealer d) {
        return dealerRepo.findById(id).map(existing -> {
            if (d.getDealerName() != null) existing.setDealerName(d.getDealerName());
            if (d.getBrand() != null) existing.setBrand(d.getBrand());
            if (d.getAddress() != null) existing.setAddress(d.getAddress());
            if (d.getCity() != null) existing.setCity(d.getCity());
            if (d.getState() != null) existing.setState(d.getState());
            if (d.getPhone() != null) existing.setPhone(d.getPhone());
            if (d.getEmail() != null) existing.setEmail(d.getEmail());
            if (d.getIsActive() != null) existing.setIsActive(d.getIsActive());
            return ResponseEntity.ok(Map.of("data", dealerRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/dealers/{id}")
    public ResponseEntity<?> deleteDealer(@PathVariable Long id) {
        dealerRepo.findById(id).ifPresent(d -> { d.setIsDeleted(true); dealerRepo.save(d); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ═══════════════════════════════════════════════════════
    // BLOG / NEWS
    // ═══════════════════════════════════════════════════════

    @GetMapping("/blog")
    public ResponseEntity<?> listPosts(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(defaultValue = "") String search) {
        if (!search.isBlank()) return ResponseEntity.ok(blogRepo.searchPosts(search, PageRequest.of(page, size, Sort.by("createdAt").descending())));
        return ResponseEntity.ok(blogRepo.findByIsDeletedFalse(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/blog/{slug}")
    public ResponseEntity<?> getPost(@PathVariable String slug) {
        return blogRepo.findBySlugAndIsDeletedFalse(slug)
                .<ResponseEntity<?>>map(post -> {
                    post.setViewCount(post.getViewCount() + 1);
                    blogRepo.save(post);
                    return ResponseEntity.ok(post);
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/blog/category/{category}")
    public ResponseEntity<?> postsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(blogRepo.findByCategoryAndStatusAndIsDeletedFalse(category, "published"));
    }

    @PostMapping("/blog")
    public ResponseEntity<?> createPost(@RequestBody BlogPost post) {
        if (post.getTitle() == null || post.getTitle().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Title required"));
        post.setId(null);
        post.setIsDeleted(false);
        if (post.getSlug() == null || post.getSlug().isBlank()) {
            post.setSlug(post.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", ""));
        }
        return ResponseEntity.ok(Map.of("data", blogRepo.save(post)));
    }

    @PutMapping("/blog/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody BlogPost p) {
        return blogRepo.findById(id).map(existing -> {
            if (p.getTitle() != null) existing.setTitle(p.getTitle());
            if (p.getSlug() != null) existing.setSlug(p.getSlug());
            if (p.getContent() != null) existing.setContent(p.getContent());
            if (p.getShortDescription() != null) existing.setShortDescription(p.getShortDescription());
            if (p.getFeaturedImage() != null) existing.setFeaturedImage(p.getFeaturedImage());
            if (p.getCategory() != null) existing.setCategory(p.getCategory());
            if (p.getTags() != null) existing.setTags(p.getTags());
            if (p.getStatus() != null) existing.setStatus(p.getStatus());
            if (p.getSeoTitle() != null) existing.setSeoTitle(p.getSeoTitle());
            if (p.getSeoDescription() != null) existing.setSeoDescription(p.getSeoDescription());
            if (p.getIsFeatured() != null) existing.setIsFeatured(p.getIsFeatured());
            return ResponseEntity.ok(Map.of("data", blogRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/blog/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        blogRepo.findById(id).ifPresent(p -> { p.setIsDeleted(true); blogRepo.save(p); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ═══════════════════════════════════════════════════════
    // DASHBOARD STATS
    // ═══════════════════════════════════════════════════════

    @GetMapping("/stats")
    public ResponseEntity<?> autoStats() {
        return ResponseEntity.ok(Map.of(
            "totalLeads", leadRepo.findByIsDeletedFalse(PageRequest.of(0, 1)).getTotalElements(),
            "newLeads", leadRepo.findByStatusAndIsDeletedFalse("new", PageRequest.of(0, 1)).getTotalElements(),
            "totalDealers", dealerRepo.findByIsDeletedFalse(PageRequest.of(0, 1)).getTotalElements(),
            "totalPosts", blogRepo.findByIsDeletedFalse(PageRequest.of(0, 1)).getTotalElements(),
            "totalComparisons", comparisonRepo.findByIsDeletedFalse(PageRequest.of(0, 1)).getTotalElements()
        ));
    }
}
