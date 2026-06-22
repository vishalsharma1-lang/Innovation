package com.cms.controller.api;

import com.cms.entity.auto.DealerDeal;
import com.cms.entity.auto.Dealer;
import com.cms.entity.auto.VehicleLead;
import com.cms.repository.auto.DealerDealRepository;
import com.cms.repository.auto.DealerRepository;
import com.cms.repository.auto.VehicleLeadRepository;
import com.cms.repository.vehicle.VehicleRepository;
import com.cms.service.DealerScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/deals")
public class DealApiController {

    @Autowired private DealerDealRepository dealRepo;
    @Autowired private DealerRepository dealerRepo;
    @Autowired private VehicleLeadRepository leadRepo;
    @Autowired private VehicleRepository vehicleRepo;
    @Autowired private DealerScraperService scraperService;

    // ─── DEALS CRUD ────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> getAllDeals() {
        return ResponseEntity.ok(dealRepo.findByIsDeletedFalseOrderByPriorityDescCreatedAtDesc());
    }

    @GetMapping("/used")
    public ResponseEntity<?> getUsedCarDeals() {
        List<Map<String, Object>> enriched = new ArrayList<>();

        // 1. Deal-based USED listings
        List<DealerDeal> deals = dealRepo.findByCarTypeAndIsActiveTrueAndIsDeletedFalse("USED");
        Set<Long> dealVehicleIds = new HashSet<>();
        for (DealerDeal d : deals) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("carType", d.getCarType());
            m.put("vehicleId", d.getVehicleId());
            m.put("vehicleName", d.getVehicleName());
            m.put("dealerName", d.getDealerName());
            m.put("city", d.getCity());
            m.put("title", d.getTitle());
            m.put("cashDiscount", d.getCashDiscount());
            m.put("ucYear", d.getUcYear());
            m.put("ucKmDriven", d.getUcKmDriven());
            m.put("ucFuelType", d.getUcFuelType());
            m.put("ucOwnerType", d.getUcOwnerType());
            m.put("ucTransmission", d.getUcTransmission());
            m.put("ucColor", d.getUcColor());
            m.put("ucAskingPrice", d.getUcAskingPrice());
            m.put("ucRegistrationState", d.getUcRegistrationState());
            m.put("ucSourceWebsite", d.getUcSourceWebsite());
            m.put("ucListingUrl", d.getUcListingUrl());
            m.put("ucDealTag", d.getUcDealTag());
            m.put("ucDealScore", d.getUcDealScore());
            m.put("priority", d.getPriority());
            m.put("isFeatured", d.getIsFeatured());
            m.put("isActive", d.getIsActive());
            String imageUrl = null;
            if (d.getVehicleId() != null) {
                dealVehicleIds.add(d.getVehicleId());
                imageUrl = vehicleRepo.findById(d.getVehicleId()).map(v -> {
                    String img = v.getHeroImage();
                    if (img == null || img.isBlank()) img = v.getThumbnailImage();
                    return img;
                }).orElse(null);
            }
            m.put("vehicleImageUrl", imageUrl);
            enriched.add(m);
        }

        // 2. USED vehicles with no deal — show them as plain listings
        vehicleRepo.findByVehicleTypeAndIsActiveTrueAndIsDeletedFalse("USED").stream()
            .filter(v -> !dealVehicleIds.contains(v.getId()))
            .forEach(v -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", "v-" + v.getId());
                m.put("carType", "USED");
                m.put("vehicleId", v.getId());
                m.put("vehicleName", v.getName());
                m.put("dealerName", null);
                m.put("city", null);
                m.put("title", v.getShortDescription());
                m.put("cashDiscount", null);
                m.put("ucYear", v.getRegistrationYear());
                m.put("ucKmDriven", v.getKmDriven());
                m.put("ucFuelType", v.getFuelType());
                m.put("ucOwnerType", v.getOwnershipCount() != null ? v.getOwnershipCount() + " Owner" : null);
                m.put("ucTransmission", v.getTransmissionType());
                m.put("ucColor", null);
                m.put("ucAskingPrice", v.getStartingPrice());
                m.put("ucRegistrationState", v.getRegistrationState());
                m.put("ucSourceWebsite", null);
                m.put("ucListingUrl", null);
                m.put("ucDealTag", "NORMAL");
                m.put("ucDealScore", null);
                m.put("priority", 0);
                m.put("isFeatured", v.getIsFeatured());
                m.put("isActive", v.getIsActive());
                String img = v.getHeroImage();
                if (img == null || img.isBlank()) img = v.getThumbnailImage();
                m.put("vehicleImageUrl", img);
                enriched.add(m);
            });

        return ResponseEntity.ok(enriched);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveDeals() {
        return ResponseEntity.ok(dealRepo.findActiveDeals());
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedDeals() {
        return ResponseEntity.ok(dealRepo.findFeaturedActiveDeals());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getDealsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(dealRepo.findByVehicleIdAndIsActiveTrueAndIsDeletedFalseOrderByTotalSavingsDesc(vehicleId));
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<?> getDealsByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(dealRepo.findActiveDealsByBrand(brand));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<?> getDealsByCity(@PathVariable String city) {
        return ResponseEntity.ok(dealRepo.findByCityAndIsActiveTrueAndIsDeletedFalseOrderByTotalSavingsDesc(city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeal(@PathVariable Long id) {
        return dealRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createDeal(@RequestBody DealerDeal deal) {
        deal.setId(null);
        if (deal.getIsDeleted() == null) deal.setIsDeleted(false);
        if (deal.getIsActive() == null) deal.setIsActive(true);
        return ResponseEntity.ok(Map.of("message", "Deal created", "data", dealRepo.save(deal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeal(@PathVariable Long id, @RequestBody DealerDeal deal) {
        return dealRepo.findById(id).map(existing -> {
            if (deal.getDealerId() != null) existing.setDealerId(deal.getDealerId());
            if (deal.getVehicleId() != null) existing.setVehicleId(deal.getVehicleId());
            if (deal.getVehicleName() != null) existing.setVehicleName(deal.getVehicleName());
            if (deal.getDealerName() != null) existing.setDealerName(deal.getDealerName());
            if (deal.getCity() != null) existing.setCity(deal.getCity());
            if (deal.getTitle() != null) existing.setTitle(deal.getTitle());
            if (deal.getDescription() != null) existing.setDescription(deal.getDescription());
            if (deal.getCashDiscount() != null) existing.setCashDiscount(deal.getCashDiscount());
            if (deal.getExchangeBonus() != null) existing.setExchangeBonus(deal.getExchangeBonus());
            if (deal.getCorporateDiscount() != null) existing.setCorporateDiscount(deal.getCorporateDiscount());
            if (deal.getFinanceBenefit() != null) existing.setFinanceBenefit(deal.getFinanceBenefit());
            if (deal.getInsuranceBenefit() != null) existing.setInsuranceBenefit(deal.getInsuranceBenefit());
            if (deal.getStartDate() != null) existing.setStartDate(deal.getStartDate());
            if (deal.getEndDate() != null) existing.setEndDate(deal.getEndDate());
            if (deal.getPriority() != null) existing.setPriority(deal.getPriority());
            if (deal.getIsFeatured() != null) existing.setIsFeatured(deal.getIsFeatured());
            if (deal.getIsActive() != null) existing.setIsActive(deal.getIsActive());
            if (deal.getCarType() != null) existing.setCarType(deal.getCarType());
            // Used car fields
            if (deal.getUcYear() != null) existing.setUcYear(deal.getUcYear());
            if (deal.getUcKmDriven() != null) existing.setUcKmDriven(deal.getUcKmDriven());
            if (deal.getUcFuelType() != null) existing.setUcFuelType(deal.getUcFuelType());
            if (deal.getUcOwnerType() != null) existing.setUcOwnerType(deal.getUcOwnerType());
            if (deal.getUcTransmission() != null) existing.setUcTransmission(deal.getUcTransmission());
            if (deal.getUcColor() != null) existing.setUcColor(deal.getUcColor());
            if (deal.getUcAskingPrice() != null) existing.setUcAskingPrice(deal.getUcAskingPrice());
            if (deal.getUcRegistrationState() != null) existing.setUcRegistrationState(deal.getUcRegistrationState());
            if (deal.getUcSourceWebsite() != null) existing.setUcSourceWebsite(deal.getUcSourceWebsite());
            if (deal.getUcListingUrl() != null) existing.setUcListingUrl(deal.getUcListingUrl());
            if (deal.getUcDealTag() != null) existing.setUcDealTag(deal.getUcDealTag());
            if (deal.getUcDealScore() != null) existing.setUcDealScore(deal.getUcDealScore());
            return ResponseEntity.ok(Map.of("data", dealRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeal(@PathVariable Long id) {
        dealRepo.findById(id).ifPresent(d -> { d.setIsDeleted(true); dealRepo.save(d); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── DEALERS CRUD ──────────────────────────────────────

    @GetMapping("/dealers")
    public ResponseEntity<?> getAllDealers() {
        return ResponseEntity.ok(dealerRepo.findByIsActiveTrueAndIsDeletedFalse());
    }

    @GetMapping("/dealers/all")
    public ResponseEntity<?> getAllDealersAdmin() {
        return ResponseEntity.ok(dealerRepo.findByIsDeletedFalse(PageRequest.of(0, 100, Sort.by("createdAt").descending())));
    }

    @PostMapping("/dealers")
    public ResponseEntity<?> createDealer(@RequestBody Dealer dealer) {
        dealer.setId(null);
        if (dealer.getIsDeleted() == null) dealer.setIsDeleted(false);
        if (dealer.getIsActive() == null) dealer.setIsActive(true);
        return ResponseEntity.ok(Map.of("message", "Dealer created", "data", dealerRepo.save(dealer)));
    }

    @PutMapping("/dealers/{id}")
    public ResponseEntity<?> updateDealer(@PathVariable Long id, @RequestBody Dealer dealer) {
        return dealerRepo.findById(id).map(existing -> {
            if (dealer.getDealerName() != null) existing.setDealerName(dealer.getDealerName());
            if (dealer.getDealerLogo() != null) existing.setDealerLogo(dealer.getDealerLogo());
            if (dealer.getBrand() != null) existing.setBrand(dealer.getBrand());
            if (dealer.getAddress() != null) existing.setAddress(dealer.getAddress());
            if (dealer.getCity() != null) existing.setCity(dealer.getCity());
            if (dealer.getState() != null) existing.setState(dealer.getState());
            if (dealer.getPincode() != null) existing.setPincode(dealer.getPincode());
            if (dealer.getPhone() != null) existing.setPhone(dealer.getPhone());
            if (dealer.getEmail() != null) existing.setEmail(dealer.getEmail());
            if (dealer.getWebsite() != null) existing.setWebsite(dealer.getWebsite());
            if (dealer.getDealerType() != null) existing.setDealerType(dealer.getDealerType());
            if (dealer.getWorkingHours() != null) existing.setWorkingHours(dealer.getWorkingHours());
            if (dealer.getIsActive() != null) existing.setIsActive(dealer.getIsActive());
            return ResponseEntity.ok(Map.of("data", dealerRepo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/dealers/{id}")
    public ResponseEntity<?> deleteDealer(@PathVariable Long id) {
        dealerRepo.findById(id).ifPresent(d -> { d.setIsDeleted(true); dealerRepo.save(d); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── LEAD MANAGEMENT ───────────────────────────────────

    @PutMapping("/leads/{id}/assign")
    public ResponseEntity<?> assignLead(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return leadRepo.findById(id).map(lead -> {
            Long dealerId = body.get("dealerId") != null ? Long.valueOf(body.get("dealerId").toString()) : null;
            lead.setDealerId(dealerId);
            lead.setContactStatus("assigned");
            if (dealerId != null) {
                dealerRepo.findById(dealerId).ifPresent(d -> lead.setDealerNameAssigned(d.getDealerName()));
            }
            return ResponseEntity.ok(Map.of("data", leadRepo.save(lead)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/leads/{id}/status")
    public ResponseEntity<?> updateLeadStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return leadRepo.findById(id).map(lead -> {
            lead.setContactStatus(body.get("status"));
            return ResponseEntity.ok(Map.of("data", leadRepo.save(lead)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── DASHBOARD STATS ───────────────────────────────────

    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVehicles", vehicleRepo.findByIsActiveTrueAndIsDeletedFalse().size());
        stats.put("totalDealers", dealerRepo.countByIsActiveTrueAndIsDeletedFalse());
        stats.put("activeDeals", dealRepo.countActiveDeals());
        stats.put("totalLeads", leadRepo.countByIsDeletedFalse());
        stats.put("convertedLeads", leadRepo.countByContactStatusAndIsDeletedFalse("converted"));
        stats.put("newLeads", leadRepo.countByContactStatusAndIsDeletedFalse("new"));
        return ResponseEntity.ok(stats);
    }

    // ─── FETCH FROM URL ────────────────────────────────────

    @PostMapping("/dealers/fetch-url")
    public ResponseEntity<?> fetchDealerFromUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        if (!url.startsWith("http")) url = "https://" + url;
        try {
            List<Map<String, String>> dealers = scraperService.fetchAllDealersFromUrl(url);
            if (dealers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No dealer information found on this page"));
            }
            return ResponseEntity.ok(Map.of("message", dealers.size() + " dealer(s) found", "data", dealers.get(0), "allDealers", dealers, "count", dealers.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch: " + e.getMessage()));
        }
    }

    @PostMapping("/fetch-deal-url")
    public ResponseEntity<?> fetchDealFromUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        if (!url.startsWith("http")) url = "https://" + url;
        try {
            Map<String, Object> data = scraperService.fetchDealFromUrl(url);
            return ResponseEntity.ok(Map.of("message", "Deal data fetched", "data", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch: " + e.getMessage()));
        }
    }
}
