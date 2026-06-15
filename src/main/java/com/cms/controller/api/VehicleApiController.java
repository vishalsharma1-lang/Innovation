package com.cms.controller.api;

import com.cms.entity.vehicle.*;
import com.cms.service.vehicle.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Complete Vehicle CMS REST API.
 * All endpoints support CRUD with soft delete.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleApiController {

    @Autowired private VehicleService vehicleService;

    // ─── VEHICLE CRUD ──────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        Page<Vehicle> result = vehicleService.searchVehicles(search,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicle(@PathVariable Long id) {
        return vehicleService.getById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<?> getVehicleFull(@PathVariable Long id) {
        VehicleService.VehicleFullData data = vehicleService.getFullVehicleData(id);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getVehicleBySlug(@PathVariable String slug) {
        return vehicleService.getBySlug(slug).map(v -> {
            VehicleService.VehicleFullData data = vehicleService.getFullVehicleData(v.getId());
            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody Vehicle vehicle) {
        if (vehicle.getName() == null || vehicle.getName().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Vehicle name is required"));
        vehicle.setId(null);
        Vehicle saved = vehicleService.save(vehicle);
        return ResponseEntity.ok(Map.of("message", "Vehicle created", "data", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return vehicleService.getById(id).map(existing -> {
            if (vehicle.getName() != null) existing.setName(vehicle.getName());
            if (vehicle.getSlug() != null) existing.setSlug(vehicle.getSlug());
            if (vehicle.getBrand() != null) existing.setBrand(vehicle.getBrand());
            if (vehicle.getModel() != null) existing.setModel(vehicle.getModel());
            if (vehicle.getModelYear() != null) existing.setModelYear(vehicle.getModelYear());
            if (vehicle.getCategory() != null) existing.setCategory(vehicle.getCategory());
            if (vehicle.getFuelType() != null) existing.setFuelType(vehicle.getFuelType());
            if (vehicle.getTransmissionType() != null) existing.setTransmissionType(vehicle.getTransmissionType());
            if (vehicle.getBodyType() != null) existing.setBodyType(vehicle.getBodyType());
            if (vehicle.getSeatingCapacity() != null) existing.setSeatingCapacity(vehicle.getSeatingCapacity());
            if (vehicle.getStartingPrice() != null) existing.setStartingPrice(vehicle.getStartingPrice());
            if (vehicle.getMaxPrice() != null) existing.setMaxPrice(vehicle.getMaxPrice());
            if (vehicle.getShortDescription() != null) existing.setShortDescription(vehicle.getShortDescription());
            if (vehicle.getFullDescription() != null) existing.setFullDescription(vehicle.getFullDescription());
            if (vehicle.getHeroImage() != null) existing.setHeroImage(vehicle.getHeroImage());
            if (vehicle.getThumbnailImage() != null) existing.setThumbnailImage(vehicle.getThumbnailImage());
            if (vehicle.getIsFeatured() != null) existing.setIsFeatured(vehicle.getIsFeatured());
            if (vehicle.getIsActive() != null) existing.setIsActive(vehicle.getIsActive());
            Vehicle saved = vehicleService.save(existing);
            return ResponseEntity.ok(Map.of("message", "Vehicle updated", "data", saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted"));
    }

    // ─── VARIANTS ──────────────────────────────────────────

    @GetMapping("/{vehicleId}/variants")
    public ResponseEntity<?> getVariants(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getVariants(vehicleId));
    }

    @PostMapping("/{vehicleId}/variants")
    public ResponseEntity<?> createVariant(@PathVariable Long vehicleId, @RequestBody VehicleVariant variant) {
        variant.setId(null);
        variant.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("message", "Variant created", "data", vehicleService.saveVariant(variant)));
    }

    @PutMapping("/variants/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable Long id, @RequestBody VehicleVariant variant) {
        return vehicleService.getVariantById(id).map(existing -> {
            if (variant.getVariantName() != null) existing.setVariantName(variant.getVariantName());
            if (variant.getFuelType() != null) existing.setFuelType(variant.getFuelType());
            if (variant.getTransmission() != null) existing.setTransmission(variant.getTransmission());
            if (variant.getEngineCC() != null) existing.setEngineCC(variant.getEngineCC());
            if (variant.getPower() != null) existing.setPower(variant.getPower());
            if (variant.getTorque() != null) existing.setTorque(variant.getTorque());
            if (variant.getMileage() != null) existing.setMileage(variant.getMileage());
            if (variant.getPrice() != null) existing.setPrice(variant.getPrice());
            if (variant.getIsActive() != null) existing.setIsActive(variant.getIsActive());
            return ResponseEntity.ok(Map.of("message", "Variant updated", "data", vehicleService.saveVariant(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/variants/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable Long id) {
        vehicleService.deleteVariant(id);
        return ResponseEntity.ok(Map.of("message", "Variant deleted"));
    }

    // ─── SPECIFICATIONS ────────────────────────────────────

    @GetMapping("/{vehicleId}/specifications")
    public ResponseEntity<?> getSpecs(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getSpecs(vehicleId));
    }

    @PostMapping("/{vehicleId}/specifications")
    public ResponseEntity<?> createSpec(@PathVariable Long vehicleId, @RequestBody VehicleSpecification spec) {
        spec.setId(null); spec.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("message", "Specification created", "data", vehicleService.saveSpec(spec)));
    }

    @PutMapping("/specifications/{id}")
    public ResponseEntity<?> updateSpec(@PathVariable Long id, @RequestBody VehicleSpecification spec) {
        return vehicleService.getSpecById(id).map(e -> {
            if (spec.getCategory() != null) e.setCategory(spec.getCategory());
            if (spec.getSpecName() != null) e.setSpecName(spec.getSpecName());
            if (spec.getSpecValue() != null) e.setSpecValue(spec.getSpecValue());
            if (spec.getDisplayOrder() != null) e.setDisplayOrder(spec.getDisplayOrder());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveSpec(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/specifications/{id}")
    public ResponseEntity<?> deleteSpec(@PathVariable Long id) {
        vehicleService.deleteSpec(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── FEATURES ──────────────────────────────────────────

    @GetMapping("/{vehicleId}/features")
    public ResponseEntity<?> getFeatures(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getFeatures(vehicleId));
    }

    @PostMapping("/{vehicleId}/features")
    public ResponseEntity<?> createFeature(@PathVariable Long vehicleId, @RequestBody VehicleFeature f) {
        f.setId(null); f.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveFeature(f)));
    }

    @PutMapping("/features/{id}")
    public ResponseEntity<?> updateFeature(@PathVariable Long id, @RequestBody VehicleFeature f) {
        return vehicleService.getFeatureById(id).map(e -> {
            if (f.getCategory() != null) e.setCategory(f.getCategory());
            if (f.getFeatureName() != null) e.setFeatureName(f.getFeatureName());
            if (f.getFeatureDescription() != null) e.setFeatureDescription(f.getFeatureDescription());
            if (f.getIconClass() != null) e.setIconClass(f.getIconClass());
            if (f.getIsAvailable() != null) e.setIsAvailable(f.getIsAvailable());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveFeature(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/features/{id}")
    public ResponseEntity<?> deleteFeature(@PathVariable Long id) {
        vehicleService.deleteFeature(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── COLORS ────────────────────────────────────────────

    @GetMapping("/{vehicleId}/colors")
    public ResponseEntity<?> getColors(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getColors(vehicleId));
    }

    @PostMapping("/{vehicleId}/colors")
    public ResponseEntity<?> createColor(@PathVariable Long vehicleId, @RequestBody VehicleColor c) {
        c.setId(null); c.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveColor(c)));
    }

    @PutMapping("/colors/{id}")
    public ResponseEntity<?> updateColor(@PathVariable Long id, @RequestBody VehicleColor c) {
        return vehicleService.getColorById(id).map(e -> {
            if (c.getColorName() != null) e.setColorName(c.getColorName());
            if (c.getColorCode() != null) e.setColorCode(c.getColorCode());
            if (c.getColorImage() != null) e.setColorImage(c.getColorImage());
            if (c.getDisplayOrder() != null) e.setDisplayOrder(c.getDisplayOrder());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveColor(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/colors/{id}")
    public ResponseEntity<?> deleteColor(@PathVariable Long id) {
        vehicleService.deleteColor(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── IMAGES ────────────────────────────────────────────

    @GetMapping("/{vehicleId}/images")
    public ResponseEntity<?> getImages(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getImages(vehicleId));
    }

    @PostMapping("/{vehicleId}/images")
    public ResponseEntity<?> createImage(@PathVariable Long vehicleId, @RequestBody VehicleImage i) {
        i.setId(null); i.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveImage(i)));
    }

    @PutMapping("/images/{id}")
    public ResponseEntity<?> updateImage(@PathVariable Long id, @RequestBody VehicleImage i) {
        return vehicleService.getImageById(id).map(e -> {
            if (i.getImageUrl() != null) e.setImageUrl(i.getImageUrl());
            if (i.getAltText() != null) e.setAltText(i.getAltText());
            if (i.getCaption() != null) e.setCaption(i.getCaption());
            if (i.getCategory() != null) e.setCategory(i.getCategory());
            if (i.getDisplayOrder() != null) e.setDisplayOrder(i.getDisplayOrder());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveImage(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        vehicleService.deleteImage(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── VIDEOS ────────────────────────────────────────────

    @GetMapping("/{vehicleId}/videos")
    public ResponseEntity<?> getVideos(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getVideos(vehicleId));
    }

    @PostMapping("/{vehicleId}/videos")
    public ResponseEntity<?> createVideo(@PathVariable Long vehicleId, @RequestBody VehicleVideo v) {
        v.setId(null); v.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveVideo(v)));
    }

    @PutMapping("/videos/{id}")
    public ResponseEntity<?> updateVideo(@PathVariable Long id, @RequestBody VehicleVideo v) {
        return vehicleService.getVideoById(id).map(e -> {
            if (v.getTitle() != null) e.setTitle(v.getTitle());
            if (v.getVideoUrl() != null) e.setVideoUrl(v.getVideoUrl());
            if (v.getThumbnailUrl() != null) e.setThumbnailUrl(v.getThumbnailUrl());
            if (v.getVideoType() != null) e.setVideoType(v.getVideoType());
            if (v.getDuration() != null) e.setDuration(v.getDuration());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveVideo(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        vehicleService.deleteVideo(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── BROCHURES ─────────────────────────────────────────

    @GetMapping("/{vehicleId}/brochures")
    public ResponseEntity<?> getBrochures(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getBrochures(vehicleId));
    }

    @PostMapping("/{vehicleId}/brochures")
    public ResponseEntity<?> createBrochure(@PathVariable Long vehicleId, @RequestBody VehicleBrochure b) {
        b.setId(null); b.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveBrochure(b)));
    }

    @DeleteMapping("/brochures/{id}")
    public ResponseEntity<?> deleteBrochure(@PathVariable Long id) {
        vehicleService.deleteBrochure(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── FAQS ──────────────────────────────────────────────

    @GetMapping("/{vehicleId}/faqs")
    public ResponseEntity<?> getFaqs(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getFaqs(vehicleId));
    }

    @PostMapping("/{vehicleId}/faqs")
    public ResponseEntity<?> createFaq(@PathVariable Long vehicleId, @RequestBody VehicleFaq f) {
        f.setId(null); f.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveFaq(f)));
    }

    @PutMapping("/faqs/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable Long id, @RequestBody VehicleFaq f) {
        return vehicleService.getFaqById(id).map(e -> {
            if (f.getQuestion() != null) e.setQuestion(f.getQuestion());
            if (f.getAnswer() != null) e.setAnswer(f.getAnswer());
            if (f.getDisplayOrder() != null) e.setDisplayOrder(f.getDisplayOrder());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveFaq(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<?> deleteFaq(@PathVariable Long id) {
        vehicleService.deleteFaq(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── REVIEWS ───────────────────────────────────────────

    @GetMapping("/{vehicleId}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(vehicleService.getReviewsPaginated(vehicleId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/{vehicleId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable Long vehicleId, @RequestBody VehicleReview r) {
        r.setId(null); r.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveReview(r)));
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody VehicleReview r) {
        return vehicleService.getReviewById(id).map(e -> {
            if (r.getReviewerName() != null) e.setReviewerName(r.getReviewerName());
            if (r.getRating() != null) e.setRating(r.getRating());
            if (r.getTitle() != null) e.setTitle(r.getTitle());
            if (r.getReviewContent() != null) e.setReviewContent(r.getReviewContent());
            if (r.getPros() != null) e.setPros(r.getPros());
            if (r.getCons() != null) e.setCons(r.getCons());
            if (r.getIsVerified() != null) e.setIsVerified(r.getIsVerified());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveReview(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        vehicleService.deleteReview(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── OFFERS ────────────────────────────────────────────

    @GetMapping("/{vehicleId}/offers")
    public ResponseEntity<?> getOffers(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getOffers(vehicleId));
    }

    @PostMapping("/{vehicleId}/offers")
    public ResponseEntity<?> createOffer(@PathVariable Long vehicleId, @RequestBody VehicleOffer o) {
        o.setId(null); o.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveOffer(o)));
    }

    @PutMapping("/offers/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Long id, @RequestBody VehicleOffer o) {
        return vehicleService.getOfferById(id).map(e -> {
            if (o.getOfferTitle() != null) e.setOfferTitle(o.getOfferTitle());
            if (o.getOfferDescription() != null) e.setOfferDescription(o.getOfferDescription());
            if (o.getDiscountAmount() != null) e.setDiscountAmount(o.getDiscountAmount());
            if (o.getDiscountType() != null) e.setDiscountType(o.getDiscountType());
            if (o.getValidFrom() != null) e.setValidFrom(o.getValidFrom());
            if (o.getValidUntil() != null) e.setValidUntil(o.getValidUntil());
            if (o.getTermsConditions() != null) e.setTermsConditions(o.getTermsConditions());
            if (o.getIsActive() != null) e.setIsActive(o.getIsActive());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveOffer(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/offers/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable Long id) {
        vehicleService.deleteOffer(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── EXPERT REVIEWS ───────────────────────────────────

    @GetMapping("/{vehicleId}/expert-reviews")
    public ResponseEntity<?> getExpertReviews(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getExpertReviews(vehicleId));
    }

    @PostMapping("/{vehicleId}/expert-reviews")
    public ResponseEntity<?> createExpertReview(@PathVariable Long vehicleId, @RequestBody VehicleExpertReview r) {
        r.setId(null); r.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveExpertReview(r)));
    }

    @PutMapping("/expert-reviews/{id}")
    public ResponseEntity<?> updateExpertReview(@PathVariable Long id, @RequestBody VehicleExpertReview r) {
        return vehicleService.getExpertReviewById(id).map(e -> {
            if (r.getReviewerName() != null) e.setReviewerName(r.getReviewerName());
            if (r.getReviewerDesignation() != null) e.setReviewerDesignation(r.getReviewerDesignation());
            if (r.getReviewerImage() != null) e.setReviewerImage(r.getReviewerImage());
            if (r.getTitle() != null) e.setTitle(r.getTitle());
            if (r.getReviewContent() != null) e.setReviewContent(r.getReviewContent());
            if (r.getPros() != null) e.setPros(r.getPros());
            if (r.getCons() != null) e.setCons(r.getCons());
            if (r.getVerdict() != null) e.setVerdict(r.getVerdict());
            if (r.getRating() != null) e.setRating(r.getRating());
            if (r.getSourceUrl() != null) e.setSourceUrl(r.getSourceUrl());
            if (r.getSourceName() != null) e.setSourceName(r.getSourceName());
            if (r.getDisplayOrder() != null) e.setDisplayOrder(r.getDisplayOrder());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveExpertReview(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/expert-reviews/{id}")
    public ResponseEntity<?> deleteExpertReview(@PathVariable Long id) {
        vehicleService.deleteExpertReview(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── NEWS & UPDATES ────────────────────────────────────

    @GetMapping("/{vehicleId}/news")
    public ResponseEntity<?> getNews(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getNews(vehicleId));
    }

    @PostMapping("/{vehicleId}/news")
    public ResponseEntity<?> createNews(@PathVariable Long vehicleId, @RequestBody VehicleNews n) {
        n.setId(null); n.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveNews(n)));
    }

    @PutMapping("/news/{id}")
    public ResponseEntity<?> updateNews(@PathVariable Long id, @RequestBody VehicleNews n) {
        return vehicleService.getNewsById(id).map(e -> {
            if (n.getTitle() != null) e.setTitle(n.getTitle());
            if (n.getContent() != null) e.setContent(n.getContent());
            if (n.getShortDescription() != null) e.setShortDescription(n.getShortDescription());
            if (n.getImageUrl() != null) e.setImageUrl(n.getImageUrl());
            if (n.getSourceUrl() != null) e.setSourceUrl(n.getSourceUrl());
            if (n.getSourceName() != null) e.setSourceName(n.getSourceName());
            if (n.getCategory() != null) e.setCategory(n.getCategory());
            if (n.getDisplayOrder() != null) e.setDisplayOrder(n.getDisplayOrder());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveNews(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        vehicleService.deleteNews(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── MODEL FAQs (from MODEL_FAQ_QUESTION_ANSWER table) ──

    @GetMapping("/model-faqs")
    public ResponseEntity<?> getAllModelFaqs() {
        return ResponseEntity.ok(vehicleService.getAllModelFaqs());
    }

    @GetMapping("/model-faqs/active")
    public ResponseEntity<?> getActiveModelFaqs() {
        return ResponseEntity.ok(vehicleService.getActiveModelFaqs());
    }

    @GetMapping("/model-faqs/active/{category}")
    public ResponseEntity<?> getActiveModelFaqsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(vehicleService.getActiveModelFaqsByCategory(category));
    }

    @PostMapping("/model-faqs")
    public ResponseEntity<?> createModelFaq(@RequestBody ModelFaq faq) {
        faq.setQuestionId(null);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveModelFaq(faq)));
    }

    @PutMapping("/model-faqs/{id}")
    public ResponseEntity<?> updateModelFaq(@PathVariable Integer id, @RequestBody ModelFaq faq) {
        return vehicleService.getModelFaqById(id).map(e -> {
            if (faq.getQuestion() != null) e.setQuestion(faq.getQuestion());
            if (faq.getAnswer() != null) e.setAnswer(faq.getAnswer());
            if (faq.getCategory() != null) e.setCategory(faq.getCategory());
            if (faq.getStartDate() != null) e.setStartDate(faq.getStartDate());
            if (faq.getEndDate() != null) e.setEndDate(faq.getEndDate());
            if (faq.getFilter() != null) e.setFilter(faq.getFilter());
            if (faq.getFilterCategory() != null) e.setFilterCategory(faq.getFilterCategory());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveModelFaq(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/model-faqs/{id}")
    public ResponseEntity<?> deleteModelFaq(@PathVariable Integer id) {
        vehicleService.deleteModelFaq(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── COMPARISONS ───────────────────────────────────────

    @GetMapping("/comparisons")
    public ResponseEntity<?> getComparisons(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(vehicleService.getComparisons(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/comparisons")
    public ResponseEntity<?> createComparison(@RequestBody VehicleComparison c) {
        c.setId(null);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveComparison(c)));
    }

    @DeleteMapping("/comparisons/{id}")
    public ResponseEntity<?> deleteComparison(@PathVariable Long id) {
        vehicleService.deleteComparison(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── PRICES ────────────────────────────────────────────

    @GetMapping("/{vehicleId}/prices")
    public ResponseEntity<?> getPrices(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.getPrices(vehicleId));
    }

    @PostMapping("/{vehicleId}/prices")
    public ResponseEntity<?> createPrice(@PathVariable Long vehicleId, @RequestBody VehiclePrice p) {
        p.setId(null); p.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.savePrice(p)));
    }

    @PutMapping("/prices/{id}")
    public ResponseEntity<?> updatePrice(@PathVariable Long id, @RequestBody VehiclePrice p) {
        return vehicleService.getPriceById(id).map(e -> {
            if (p.getCity() != null) e.setCity(p.getCity());
            if (p.getState() != null) e.setState(p.getState());
            if (p.getExShowroomPrice() != null) e.setExShowroomPrice(p.getExShowroomPrice());
            if (p.getOnRoadPrice() != null) e.setOnRoadPrice(p.getOnRoadPrice());
            if (p.getRto() != null) e.setRto(p.getRto());
            if (p.getInsurance() != null) e.setInsurance(p.getInsurance());
            return ResponseEntity.ok(Map.of("data", vehicleService.savePrice(e)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/prices/{id}")
    public ResponseEntity<?> deletePrice(@PathVariable Long id) {
        vehicleService.deletePrice(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ─── SEO ───────────────────────────────────────────────

    @GetMapping("/{vehicleId}/seo")
    public ResponseEntity<?> getSeo(@PathVariable Long vehicleId) {
        return vehicleService.getSeo(vehicleId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("message", "No SEO settings yet")));
    }

    @PostMapping("/{vehicleId}/seo")
    public ResponseEntity<?> createOrUpdateSeo(@PathVariable Long vehicleId, @RequestBody VehicleSeo seo) {
        // Upsert — if SEO exists for this vehicle, update it
        VehicleSeo existing = vehicleService.getSeo(vehicleId).orElse(null);
        if (existing != null) {
            if (seo.getPageTitle() != null) existing.setPageTitle(seo.getPageTitle());
            if (seo.getMetaDescription() != null) existing.setMetaDescription(seo.getMetaDescription());
            if (seo.getMetaKeywords() != null) existing.setMetaKeywords(seo.getMetaKeywords());
            if (seo.getCanonicalUrl() != null) existing.setCanonicalUrl(seo.getCanonicalUrl());
            if (seo.getOgTitle() != null) existing.setOgTitle(seo.getOgTitle());
            if (seo.getOgDescription() != null) existing.setOgDescription(seo.getOgDescription());
            if (seo.getOgImage() != null) existing.setOgImage(seo.getOgImage());
            if (seo.getRobots() != null) existing.setRobots(seo.getRobots());
            return ResponseEntity.ok(Map.of("data", vehicleService.saveSeo(existing)));
        }
        seo.setId(null);
        seo.setVehicleId(vehicleId);
        return ResponseEntity.ok(Map.of("data", vehicleService.saveSeo(seo)));
    }

    @DeleteMapping("/seo/{id}")
    public ResponseEntity<?> deleteSeo(@PathVariable Long id) {
        vehicleService.deleteSeo(id); return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
