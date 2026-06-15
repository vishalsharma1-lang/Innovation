package com.cms.service.vehicle;

import com.cms.entity.vehicle.*;
import com.cms.repository.vehicle.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehicleService {

    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private VehicleVariantRepository variantRepo;
    @Autowired private VehicleSpecificationRepository specRepo;
    @Autowired private VehicleFeatureRepository featureRepo;
    @Autowired private VehicleColorRepository colorRepo;
    @Autowired private VehicleImageRepository imageRepo;
    @Autowired private VehicleVideoRepository videoRepo;
    @Autowired private VehicleBrochureRepository brochureRepo;
    @Autowired private VehicleFaqRepository faqRepo;
    @Autowired private ModelFaqRepository modelFaqRepo;
    @Autowired private VehicleReviewRepository reviewRepo;
    @Autowired private VehicleExpertReviewRepository expertReviewRepo;
    @Autowired private VehicleNewsRepository newsRepo;
    @Autowired private VehicleOfferRepository offerRepo;
    @Autowired private VehicleComparisonRepository comparisonRepo;
    @Autowired private VehiclePriceRepository priceRepo;
    @Autowired private VehicleSeoRepository seoRepo;

    // ─── Vehicle CRUD ─────────────────────────────────────

    public Page<Vehicle> searchVehicles(String search, Pageable pageable) {
        if (search == null || search.isBlank()) return vehicleRepository.findByIsDeletedFalse(pageable);
        return vehicleRepository.searchVehicles(search.trim(), pageable);
    }

    public List<Vehicle> searchActiveVehicles(String search) {
        if (search == null || search.isBlank()) return getActiveVehicles();
        return vehicleRepository.searchActiveVehicles(search.trim());
    }

    public List<Vehicle> getActiveVehicles() { return vehicleRepository.findByIsActiveTrueAndIsDeletedFalse(); }
    public List<Vehicle> getFeaturedVehicles() { return vehicleRepository.findByIsFeaturedTrueAndIsActiveTrueAndIsDeletedFalse(); }
    public Optional<Vehicle> getById(Long id) { return vehicleRepository.findByIdAndIsDeletedFalse(id); }
    public Optional<Vehicle> getBySlug(String slug) { return vehicleRepository.findFirstBySlugAndIsDeletedFalse(slug); }
    public List<Vehicle> getVehiclesByBrand(String brand) { return vehicleRepository.findByBrandIgnoreCase(brand); }
    public List<String> getDistinctBrands() { return vehicleRepository.findDistinctBrands(); }

    public Vehicle save(Vehicle v) {
        if (v.getSlug() == null || v.getSlug().isBlank()) {
            v.setSlug(v.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", ""));
        }
        if (v.getIsActive() == null) v.setIsActive(true);
        if (v.getIsDeleted() == null) v.setIsDeleted(false);
        if (v.getVehicleType() == null || v.getVehicleType().isBlank()) v.setVehicleType("NEW");
        return vehicleRepository.save(v);
    }

    // ─── Used Car helpers ──────────────────────────────────
    public List<Vehicle> getUsedCars() {
        return vehicleRepository.findByVehicleTypeAndIsActiveTrueAndIsDeletedFalse("USED");
    }

    public List<Vehicle> getNewCars() {
        return vehicleRepository.findByVehicleTypeAndIsActiveTrueAndIsDeletedFalse("NEW");
    }

    public Page<Vehicle> getUsedCarsPaged(String search, Pageable pageable) {
        if (search == null || search.isBlank())
            return vehicleRepository.findByTypePageable("USED", pageable);
        return vehicleRepository.searchByTypePageable("USED", search.trim(), pageable);
    }

    public long countByType(String type) { return vehicleRepository.countByType(type); }
    public long countAll() { return vehicleRepository.countByIsDeletedFalse(); }

    public Page<Vehicle> searchVehiclesByType(String type, String search, Pageable pageable) {
        if (search == null || search.isBlank())
            return vehicleRepository.findByTypeAndIsDeletedFalsePageable(type, pageable);
        return vehicleRepository.searchByTypeAndIsDeletedFalsePageable(type, search.trim(), pageable);
    }

    public void delete(Long id) {
        vehicleRepository.findById(id).ifPresent(v -> { v.setIsDeleted(true); vehicleRepository.save(v); });
    }

    // ─── Generic sub-entity CRUD helpers ──────────────────

    // Variants
    public List<VehicleVariant> getVariants(Long vehicleId) { return variantRepo.findByVehicleIdAndIsDeletedFalseOrderByPriceAsc(vehicleId); }
    public Optional<VehicleVariant> getVariantById(Long id) { return variantRepo.findById(id); }
    public VehicleVariant saveVariant(VehicleVariant v) { if(v.getIsDeleted()==null) v.setIsDeleted(false); return variantRepo.save(v); }
    public void deleteVariant(Long id) { variantRepo.findById(id).ifPresent(v -> { v.setIsDeleted(true); variantRepo.save(v); }); }

    // Specifications
    public List<VehicleSpecification> getSpecs(Long vehicleId) { return specRepo.findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(vehicleId); }
    public Optional<VehicleSpecification> getSpecById(Long id) { return specRepo.findById(id); }
    public VehicleSpecification saveSpec(VehicleSpecification s) { if(s.getIsDeleted()==null) s.setIsDeleted(false); return specRepo.save(s); }
    public void deleteSpec(Long id) { specRepo.findById(id).ifPresent(s -> { s.setIsDeleted(true); specRepo.save(s); }); }

    // Features
    public List<VehicleFeature> getFeatures(Long vehicleId) { return featureRepo.findByVehicleIdAndIsDeletedFalseOrderByCategoryAsc(vehicleId); }
    public Optional<VehicleFeature> getFeatureById(Long id) { return featureRepo.findById(id); }
    public VehicleFeature saveFeature(VehicleFeature f) { if(f.getIsDeleted()==null) f.setIsDeleted(false); return featureRepo.save(f); }
    public void deleteFeature(Long id) { featureRepo.findById(id).ifPresent(f -> { f.setIsDeleted(true); featureRepo.save(f); }); }

    // Colors
    public List<VehicleColor> getColors(Long vehicleId) { return colorRepo.findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(vehicleId); }
    public Optional<VehicleColor> getColorById(Long id) { return colorRepo.findById(id); }
    public VehicleColor saveColor(VehicleColor c) { if(c.getIsDeleted()==null) c.setIsDeleted(false); return colorRepo.save(c); }
    public void deleteColor(Long id) { colorRepo.findById(id).ifPresent(c -> { c.setIsDeleted(true); colorRepo.save(c); }); }

    // Images - vehicle/exterior images appear first, then others
    public List<VehicleImage> getImages(Long vehicleId) {
        List<VehicleImage> images = imageRepo.findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(vehicleId);
        // Remove images with blank/null URLs
        images.removeIf(i -> i.getImageUrl() == null || i.getImageUrl().isBlank());
        // Sort: vehicle/exterior/car categories first, then others
        images.sort((a, b) -> {
            int priorityA = getImageCategoryPriority(a.getCategory());
            int priorityB = getImageCategoryPriority(b.getCategory());
            if (priorityA != priorityB) return Integer.compare(priorityA, priorityB);
            // Within same priority, keep original display order
            int orderA = a.getDisplayOrder() != null ? a.getDisplayOrder() : Integer.MAX_VALUE;
            int orderB = b.getDisplayOrder() != null ? b.getDisplayOrder() : Integer.MAX_VALUE;
            return Integer.compare(orderA, orderB);
        });
        return images;
    }

    private int getImageCategoryPriority(String category) {
        if (category == null) return 3;
        String cat = category.toLowerCase();
        if (cat.contains("exterior") || cat.contains("car") || cat.contains("vehicle")) return 0;
        if (cat.contains("interior")) return 1;
        if (cat.contains("gallery")) return 2;
        if (cat.contains("logo") || cat.contains("brand")) return 4;
        return 3; // other categories
    }
    public Optional<VehicleImage> getImageById(Long id) { return imageRepo.findById(id); }
    public VehicleImage saveImage(VehicleImage i) { if(i.getIsDeleted()==null) i.setIsDeleted(false); return imageRepo.save(i); }
    public void deleteImage(Long id) { imageRepo.findById(id).ifPresent(i -> { i.setIsDeleted(true); imageRepo.save(i); }); }

    // Videos
    public List<VehicleVideo> getVideos(Long vehicleId) { return videoRepo.findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(vehicleId); }
    public Optional<VehicleVideo> getVideoById(Long id) { return videoRepo.findById(id); }
    public VehicleVideo saveVideo(VehicleVideo v) { if(v.getIsDeleted()==null) v.setIsDeleted(false); return videoRepo.save(v); }
    public void deleteVideo(Long id) { videoRepo.findById(id).ifPresent(v -> { v.setIsDeleted(true); videoRepo.save(v); }); }

    // Brochures
    public List<VehicleBrochure> getBrochures(Long vehicleId) { return brochureRepo.findByVehicleIdAndIsDeletedFalse(vehicleId); }
    public Optional<VehicleBrochure> getBrochureById(Long id) { return brochureRepo.findById(id); }
    public VehicleBrochure saveBrochure(VehicleBrochure b) { if(b.getIsDeleted()==null) b.setIsDeleted(false); return brochureRepo.save(b); }
    public void deleteBrochure(Long id) { brochureRepo.findById(id).ifPresent(b -> { b.setIsDeleted(true); brochureRepo.save(b); }); }

    // FAQs
    public List<VehicleFaq> getFaqs(Long vehicleId) { return faqRepo.findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(vehicleId); }
    public Optional<VehicleFaq> getFaqById(Long id) { return faqRepo.findById(id); }
    public VehicleFaq saveFaq(VehicleFaq f) { if(f.getIsDeleted()==null) f.setIsDeleted(false); return faqRepo.save(f); }
    public void deleteFaq(Long id) { faqRepo.findById(id).ifPresent(f -> { f.setIsDeleted(true); faqRepo.save(f); }); }

    // Model FAQs (from MODEL_FAQ_QUESTION_ANSWER table)
    public List<ModelFaq> getAllModelFaqs() { return modelFaqRepo.findAllByOrderByQuestionIdAsc(); }
    public List<ModelFaq> getActiveModelFaqs() { return modelFaqRepo.findActiveFaqs(); }
    public List<ModelFaq> getActiveModelFaqsByCategory(String category) { return modelFaqRepo.findActiveFaqsByCategory(category); }
    public List<ModelFaq> getActiveModelFaqsByFilter(String filter) { return modelFaqRepo.findActiveFaqsByFilter(filter); }
    public Optional<ModelFaq> getModelFaqById(Integer id) { return modelFaqRepo.findById(id); }
    public ModelFaq saveModelFaq(ModelFaq f) { return modelFaqRepo.save(f); }
    public void deleteModelFaq(Integer id) { modelFaqRepo.deleteById(id); }

    /**
     * Get active model FAQs relevant to a vehicle (based on model status filter).
     * Returns general FAQs (empty filter) and model-specific ones.
     */
    public List<ModelFaq> getActiveModelFaqsForVehicle(Vehicle vehicle) {
        List<ModelFaq> allActive = modelFaqRepo.findActiveFaqs();
        List<ModelFaq> relevant = new ArrayList<>();
        for (ModelFaq faq : allActive) {
            String filter = faq.getFilter();
            String filterCategory = faq.getFilterCategory();
            // Include FAQs with empty/null filter (general FAQs) or matching "current" modelstatus
            if (filter == null || filter.isEmpty() || "current".equalsIgnoreCase(filter)) {
                // Exclude page-specific FAQs like EMI calculator, brand page, etc.
                if (filterCategory == null || filterCategory.isEmpty() || "modelstatus".equalsIgnoreCase(filterCategory)) {
                    relevant.add(faq);
                }
            }
        }
        return relevant;
    }

    // Reviews
    public List<VehicleReview> getReviews(Long vehicleId) { return reviewRepo.findByVehicleIdAndIsDeletedFalseOrderByCreatedAtDesc(vehicleId); }
    public Page<VehicleReview> getReviewsPaginated(Long vehicleId, Pageable pageable) { return reviewRepo.findByVehicleIdAndIsDeletedFalse(vehicleId, pageable); }
    public Optional<VehicleReview> getReviewById(Long id) { return reviewRepo.findById(id); }
    public VehicleReview saveReview(VehicleReview r) { if(r.getIsDeleted()==null) r.setIsDeleted(false); return reviewRepo.save(r); }
    public void deleteReview(Long id) { reviewRepo.findById(id).ifPresent(r -> { r.setIsDeleted(true); reviewRepo.save(r); }); }

    // Expert Reviews
    public List<VehicleExpertReview> getExpertReviews(Long vehicleId) { return expertReviewRepo.findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(vehicleId); }
    public Optional<VehicleExpertReview> getExpertReviewById(Long id) { return expertReviewRepo.findById(id); }
    public VehicleExpertReview saveExpertReview(VehicleExpertReview r) { if(r.getIsDeleted()==null) r.setIsDeleted(false); return expertReviewRepo.save(r); }
    public void deleteExpertReview(Long id) { expertReviewRepo.findById(id).ifPresent(r -> { r.setIsDeleted(true); expertReviewRepo.save(r); }); }

    // News & Updates
    public List<VehicleNews> getNews(Long vehicleId) { return newsRepo.findByVehicleIdAndIsDeletedFalseOrderByPublishedDateDesc(vehicleId); }
    public Optional<VehicleNews> getNewsById(Long id) { return newsRepo.findById(id); }
    public VehicleNews saveNews(VehicleNews n) { if(n.getIsDeleted()==null) n.setIsDeleted(false); return newsRepo.save(n); }
    public void deleteNews(Long id) { newsRepo.findById(id).ifPresent(n -> { n.setIsDeleted(true); newsRepo.save(n); }); }

    // Offers
    public List<VehicleOffer> getOffers(Long vehicleId) { return offerRepo.findByVehicleIdAndIsActiveTrueAndIsDeletedFalse(vehicleId); }
    public Optional<VehicleOffer> getOfferById(Long id) { return offerRepo.findById(id); }
    public VehicleOffer saveOffer(VehicleOffer o) { if(o.getIsDeleted()==null) o.setIsDeleted(false); return offerRepo.save(o); }
    public void deleteOffer(Long id) { offerRepo.findById(id).ifPresent(o -> { o.setIsDeleted(true); offerRepo.save(o); }); }

    // Comparisons
    public Page<VehicleComparison> getComparisons(Pageable pageable) { return comparisonRepo.findByIsDeletedFalse(pageable); }
    public Optional<VehicleComparison> getComparisonById(Long id) { return comparisonRepo.findById(id); }
    public VehicleComparison saveComparison(VehicleComparison c) { if(c.getIsDeleted()==null) c.setIsDeleted(false); return comparisonRepo.save(c); }
    public void deleteComparison(Long id) { comparisonRepo.findById(id).ifPresent(c -> { c.setIsDeleted(true); comparisonRepo.save(c); }); }

    // Prices
    public List<VehiclePrice> getPrices(Long vehicleId) { return priceRepo.findByVehicleIdAndIsDeletedFalse(vehicleId); }
    public Optional<VehiclePrice> getPriceById(Long id) { return priceRepo.findById(id); }
    public VehiclePrice savePrice(VehiclePrice p) { if(p.getIsDeleted()==null) p.setIsDeleted(false); return priceRepo.save(p); }
    public void deletePrice(Long id) { priceRepo.findById(id).ifPresent(p -> { p.setIsDeleted(true); priceRepo.save(p); }); }

    // SEO
    public Optional<VehicleSeo> getSeo(Long vehicleId) { return seoRepo.findByVehicleIdAndIsDeletedFalse(vehicleId); }
    public Optional<VehicleSeo> getSeoById(Long id) { return seoRepo.findById(id); }
    public VehicleSeo saveSeo(VehicleSeo s) { if(s.getIsDeleted()==null) s.setIsDeleted(false); return seoRepo.save(s); }
    public void deleteSeo(Long id) { seoRepo.findById(id).ifPresent(s -> { s.setIsDeleted(true); seoRepo.save(s); }); }

    // ─── Full vehicle data (for website rendering) ────────

    public VehicleFullData getFullVehicleData(Long vehicleId) {
        VehicleFullData data = new VehicleFullData();
        data.vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId).orElse(null);
        if (data.vehicle == null) return null;
        data.variants = getVariants(vehicleId);
        data.specifications = getSpecs(vehicleId);
        data.features = getFeatures(vehicleId);
        data.colors = getColors(vehicleId);
        data.images = getImages(vehicleId);
        data.videos = getVideos(vehicleId);
        data.brochures = getBrochures(vehicleId);
        data.faqs = getFaqs(vehicleId);
        data.modelFaqs = getActiveModelFaqsForVehicle(data.vehicle);
        data.reviews = getReviews(vehicleId);
        data.expertReviews = getExpertReviews(vehicleId);
        data.news = getNews(vehicleId);
        data.offers = getOffers(vehicleId);
        data.prices = getPrices(vehicleId);
        data.seo = getSeo(vehicleId).orElse(null);
        return data;
    }

    // DTO for full page data
    public static class VehicleFullData {
        public Vehicle vehicle;
        public List<VehicleVariant> variants;
        public List<VehicleSpecification> specifications;
        public List<VehicleFeature> features;
        public List<VehicleColor> colors;
        public List<VehicleImage> images;
        public List<VehicleVideo> videos;
        public List<VehicleBrochure> brochures;
        public List<VehicleFaq> faqs;
        public List<ModelFaq> modelFaqs;
        public List<VehicleReview> reviews;
        public List<VehicleExpertReview> expertReviews;
        public List<VehicleNews> news;
        public List<VehicleOffer> offers;
        public List<VehiclePrice> prices;
        public VehicleSeo seo;
    }
}
