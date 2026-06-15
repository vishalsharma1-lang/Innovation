package com.cms.controller;

import com.cms.entity.auto.DealerDeal;
import com.cms.entity.vehicle.Vehicle;
import com.cms.entity.vehicle.VehicleImage;
import com.cms.repository.auto.DealerDealRepository;
import com.cms.repository.auto.DealerRepository;
import com.cms.service.vehicle.VehicleService;
import com.cms.service.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class VehicleWebsiteController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private DealerDealRepository dealRepo;

    @Autowired
    private DealerRepository dealerRepo;

    @GetMapping("/vehicles")
    public String vehicleListPage(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Vehicle> vehicles;
        if (search != null && !search.isBlank()) {
            vehicles = vehicleService.searchActiveVehicles(search);
            model.addAttribute("searchQuery", search);
        } else {
            vehicles = vehicleService.getActiveVehicles();
        }
        // Ensure each vehicle has an image for display - fallback to first VehicleImage
        for (Vehicle v : vehicles) {
            if ((v.getHeroImage() == null || v.getHeroImage().isBlank()) &&
                (v.getThumbnailImage() == null || v.getThumbnailImage().isBlank())) {
                List<VehicleImage> images = vehicleService.getImages(v.getId());
                if (!images.isEmpty()) {
                    v.setHeroImage(images.get(0).getImageUrl());
                }
            }
        }
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("featured", vehicleService.getFeaturedVehicles());
        model.addAttribute("brands", vehicleService.getDistinctBrands());
        // Deal data for vehicle cards
        model.addAttribute("vehicleDeals", getVehicleDealsMap(vehicles));
        // Fetch SEO from SEO Management (page name = "vehicles")
        seoService.getSeoByPage("vehicles").ifPresent(seo -> model.addAttribute("seo", seo));
        return "website/vehicles";
    }

    // Brand listing page: /brand/hyundai, /brand/maruti-suzuki
    @GetMapping("/brand/{brandSlug}")
    public String brandPage(@PathVariable String brandSlug, Model model) {
        // Convert slug to brand name (e.g. maruti-suzuki -> Maruti Suzuki)
        String brandName = brandSlug.replace("-", " ");

        // Try exact slug match first, then try title-cased version
        List<Vehicle> vehicles = vehicleService.getVehiclesByBrand(brandName);

        // If no results, try with each word capitalized
        if (vehicles.isEmpty()) {
            String titleCase = java.util.Arrays.stream(brandName.split(" "))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .reduce((a, b) -> a + " " + b).orElse(brandName);
            vehicles = vehicleService.getVehiclesByBrand(titleCase);
            if (!vehicles.isEmpty()) brandName = titleCase;
        }

        // If still no results, try finding the brand from all brands
        if (vehicles.isEmpty()) {
            List<String> allBrands = vehicleService.getDistinctBrands();
            for (String b : allBrands) {
                if (b.toLowerCase().replace(" ", "-").equals(brandSlug.toLowerCase())) {
                    vehicles = vehicleService.getVehiclesByBrand(b);
                    brandName = b;
                    break;
                }
            }
        }

        model.addAttribute("brandSlug", brandSlug);
        model.addAttribute("brandName", brandName.substring(0, 1).toUpperCase() + brandName.substring(1));
        // Ensure each vehicle has a thumbnail - fallback to first VehicleImage if needed
        for (Vehicle v : vehicles) {
            if (v.getThumbnailImage() == null || v.getThumbnailImage().isBlank()) {
                List<VehicleImage> imgs = vehicleService.getImages(v.getId());
                if (!imgs.isEmpty()) {
                    v.setThumbnailImage(imgs.get(0).getImageUrl());
                }
            }
        }
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("vehicleCount", vehicles.size());
        model.addAttribute("allBrands", vehicleService.getDistinctBrands());
        return "website/brand";
    }

    // URL: /cars/tata/nexon (like cardekho.com/tata/nexon)
    @GetMapping("/cars/{brand}/{model}")
    public String vehicleByBrandModel(@PathVariable String brand, @PathVariable String model, Model m) {
        String slug = brand.toLowerCase() + "-" + model.toLowerCase();
        return loadVehicleDetail(slug, m);
    }

    // URL: /vehicles/tata/nexon (requested format)
    @GetMapping("/vehicles/{brand}/{slug}")
    public String vehicleByBrandSlug(@PathVariable String brand, @PathVariable String slug, Model m) {
        // Try brand-slug combo first
        String fullSlug = brand.toLowerCase() + "-" + slug.toLowerCase();
        if (vehicleService.getBySlug(fullSlug).isPresent()) {
            return loadVehicleDetail(fullSlug, m);
        }
        // Fallback to just slug
        return loadVehicleDetail(slug, m);
    }

    // URL: /vehicles/tata-nexon (slug-based fallback)
    @GetMapping("/vehicles/{slug}")
    public String vehicleBySlug(@PathVariable String slug, Model m) {
        return loadVehicleDetail(slug, m);
    }

    private String loadVehicleDetail(String slug, Model m) {
        return vehicleService.getBySlug(slug).map(vehicle -> {
            VehicleService.VehicleFullData data = vehicleService.getFullVehicleData(vehicle.getId());
            m.addAttribute("v", data.vehicle);
            m.addAttribute("variants", data.variants);
            m.addAttribute("specifications", data.specifications);
            m.addAttribute("features", data.features);
            m.addAttribute("colors", data.colors);
            m.addAttribute("images", data.images);
            m.addAttribute("videos", data.videos);
            m.addAttribute("brochures", data.brochures);
            m.addAttribute("faqs", data.faqs);
            m.addAttribute("modelFaqs", data.modelFaqs);
            m.addAttribute("reviews", data.reviews);
            m.addAttribute("expertReviews", data.expertReviews);
            m.addAttribute("news", data.news);
            m.addAttribute("offers", data.offers);
            m.addAttribute("prices", data.prices);
            m.addAttribute("seo", data.seo);

            // YouTube Reels for this vehicle
            if (Boolean.TRUE.equals(data.vehicle.getYoutubeEnabled()) && data.vehicle.getYoutubeChannelId() != null) {
                m.addAttribute("youtubeEnabled", true);
                m.addAttribute("youtubeChannelId", data.vehicle.getYoutubeChannelId());
                m.addAttribute("youtubeLayout", data.vehicle.getYoutubeLayout() != null ? data.vehicle.getYoutubeLayout() : "grid");
                m.addAttribute("youtubeLimit", data.vehicle.getYoutubeLimit() != null ? data.vehicle.getYoutubeLimit() : 6);
            }

            // Dealer deals for this vehicle
            List<DealerDeal> vehicleDeals = dealRepo.findByVehicleIdAndIsActiveTrueAndIsDeletedFalseOrderByTotalSavingsDesc(vehicle.getId());
            m.addAttribute("dealerDeals", vehicleDeals);

            return "website/vehicle-detail";
        }).orElse("redirect:/vehicles");
    }

    /**
     * Build a map of vehicleId -> deal summary (maxSavings, dealerCount) for listing pages.
     */
    private Map<Long, Map<String, Object>> getVehicleDealsMap(List<Vehicle> vehicles) {
        List<DealerDeal> allActiveDeals = dealRepo.findActiveDeals();
        Map<Long, Map<String, Object>> dealsMap = new HashMap<>();
        for (Vehicle v : vehicles) {
            List<DealerDeal> vDeals = allActiveDeals.stream()
                .filter(d -> v.getId().equals(d.getVehicleId()))
                .collect(Collectors.toList());
            if (!vDeals.isEmpty()) {
                Map<String, Object> summary = new HashMap<>();
                BigDecimal maxSavings = vDeals.stream()
                    .map(DealerDeal::getTotalSavings)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
                long dealerCount = vDeals.stream()
                    .map(DealerDeal::getDealerId)
                    .filter(Objects::nonNull)
                    .distinct().count();
                summary.put("maxSavings", maxSavings);
                summary.put("dealerCount", dealerCount);
                summary.put("dealCount", vDeals.size());
                dealsMap.put(v.getId(), summary);
            }
        }
        return dealsMap;
    }
}
