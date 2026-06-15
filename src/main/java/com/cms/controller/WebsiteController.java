package com.cms.controller;

import com.cms.entity.BannerSettings;
import com.cms.entity.ContentSettings;
import com.cms.entity.SeoSettings;
import com.cms.entity.auto.DealerDeal;
import com.cms.repository.auto.DealerDealRepository;
import com.cms.service.BannerService;
import com.cms.service.ContentService;
import com.cms.service.SeoService;
import com.cms.service.vehicle.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebsiteController {

    @Autowired private SeoService seoService;
    @Autowired private ContentService contentService;
    @Autowired private BannerService bannerService;
    @Autowired private VehicleService vehicleService;
    @Autowired private DealerDealRepository dealRepo;

    // Brand logo mapping - maps brand names (lowercase) to logo URLs
    private static final Map<String, String> BRAND_LOGOS = new HashMap<>();
    static {
        BRAND_LOGOS.put("maruti suzuki", "https://imgd.aeplcdn.com/0x0/cw/brands/maruti-suzuki.png");
        BRAND_LOGOS.put("hyundai", "https://imgd.aeplcdn.com/0x0/cw/brands/hyundai.png");
        BRAND_LOGOS.put("tata", "https://imgd.aeplcdn.com/0x0/cw/brands/tata.png");
        BRAND_LOGOS.put("mahindra", "https://imgd.aeplcdn.com/0x0/cw/brands/mahindra.png");
        BRAND_LOGOS.put("kia", "https://imgd.aeplcdn.com/0x0/cw/brands/kia.png");
        BRAND_LOGOS.put("toyota", "https://imgd.aeplcdn.com/0x0/cw/brands/toyota.png");
        BRAND_LOGOS.put("honda", "https://imgd.aeplcdn.com/0x0/cw/brands/honda.png");
        BRAND_LOGOS.put("mg", "https://imgd.aeplcdn.com/0x0/cw/brands/mg.png");
        BRAND_LOGOS.put("volkswagen", "https://imgd.aeplcdn.com/0x0/cw/brands/volkswagen.png");
        BRAND_LOGOS.put("skoda", "https://imgd.aeplcdn.com/0x0/cw/brands/skoda.png");
        BRAND_LOGOS.put("renault", "https://imgd.aeplcdn.com/0x0/cw/brands/renault.png");
        BRAND_LOGOS.put("nissan", "https://imgd.aeplcdn.com/0x0/cw/brands/nissan.png");
        BRAND_LOGOS.put("ford", "https://imgd.aeplcdn.com/0x0/cw/brands/ford.png");
        BRAND_LOGOS.put("bmw", "https://imgd.aeplcdn.com/0x0/cw/brands/bmw.png");
        BRAND_LOGOS.put("mercedes-benz", "https://imgd.aeplcdn.com/0x0/cw/brands/mercedes-benz.png");
        BRAND_LOGOS.put("audi", "https://imgd.aeplcdn.com/0x0/cw/brands/audi.png");
        BRAND_LOGOS.put("jeep", "https://imgd.aeplcdn.com/0x0/cw/brands/jeep.png");
        BRAND_LOGOS.put("citroen", "https://imgd.aeplcdn.com/0x0/cw/brands/citroen.png");
        BRAND_LOGOS.put("volvo", "https://imgd.aeplcdn.com/0x0/cw/brands/volvo.png");
        BRAND_LOGOS.put("lexus", "https://imgd.aeplcdn.com/0x0/cw/brands/lexus.png");
    }

    private void addPageData(Model model, String pageName) {
        // SEO data
        SeoSettings seo = seoService.getSeoByPage(pageName).orElse(null);
        model.addAttribute("seo", seo);

        // Content data
        List<ContentSettings> contentList = contentService.getContentByPage(pageName);
        model.addAttribute("contentList", contentList);
        // Expose each section as a named attribute (lowercase key)
        contentList.forEach(c -> model.addAttribute("content_" + c.getSectionName().toLowerCase(), c));

        // Banners
        List<BannerSettings> banners = bannerService.getBannersByPage(pageName);
        model.addAttribute("banners", banners);
        if (!banners.isEmpty()) {
            model.addAttribute("primaryBanner", banners.get(0));
        }
    }

    @GetMapping({"/", "/index"})
    public String home(Model model) {
        addPageData(model, "home");
        List<String> brands = vehicleService.getDistinctBrands();
        model.addAttribute("brands", brands);

        // Provide brand logo map for the template
        Map<String, String> brandLogos = new HashMap<>();
        for (String brand : brands) {
            String logo = BRAND_LOGOS.get(brand.toLowerCase());
            if (logo != null) {
                brandLogos.put(brand, logo);
            }
        }
        model.addAttribute("brandLogos", brandLogos);

        // Featured deals for homepage
        List<DealerDeal> featuredDeals = dealRepo.findFeaturedActiveDeals();
        if (featuredDeals.isEmpty()) featuredDeals = dealRepo.findActiveDeals();
        List<DealerDeal> topDeals = featuredDeals.size() > 6 ? featuredDeals.subList(0, 6) : featuredDeals;
        model.addAttribute("topDeals", topDeals);

        // Map vehicleId to slug and heroImage for deal cards
        Map<Long, String> vehicleSlugs = new HashMap<>();
        Map<Long, String> vehicleImages = new HashMap<>();
        for (DealerDeal deal : topDeals) {
            if (deal.getVehicleId() != null) {
                vehicleService.getById(deal.getVehicleId()).ifPresent(v -> {
                    vehicleSlugs.put(v.getId(), v.getSlug());
                    String img = v.getHeroImage();
                    if (img == null || img.isBlank()) img = v.getThumbnailImage();
                    if (img == null || img.isBlank()) {
                        img = vehicleService.getImages(v.getId()).stream()
                                .findFirst().map(i -> i.getImageUrl()).orElse(null);
                    }
                    if (img != null) vehicleImages.put(v.getId(), img);
                });
            }
        }
        model.addAttribute("vehicleSlugs", vehicleSlugs);
        model.addAttribute("vehicleImages", vehicleImages);

        // Featured vehicles for homepage
        List<com.cms.entity.vehicle.Vehicle> featuredVehicles = vehicleService.getFeaturedVehicles();
        if (featuredVehicles.isEmpty()) {
            List<com.cms.entity.vehicle.Vehicle> all = vehicleService.getActiveVehicles();
            featuredVehicles = all.size() > 8 ? all.subList(0, 8) : all;
        }
        for (com.cms.entity.vehicle.Vehicle v : featuredVehicles) {
            if ((v.getHeroImage() == null || v.getHeroImage().isBlank()) &&
                (v.getThumbnailImage() == null || v.getThumbnailImage().isBlank())) {
                vehicleService.getImages(v.getId()).stream().findFirst()
                    .ifPresent(img -> v.setHeroImage(img.getImageUrl()));
            }
        }
        model.addAttribute("featuredVehicles", featuredVehicles);
        model.addAttribute("totalVehicles", vehicleService.getActiveVehicles().size());

        return "website/index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        addPageData(model, "about");
        return "website/about";
    }

    @GetMapping("/services")
    public String services(Model model) {
        addPageData(model, "services");
        return "website/services";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        addPageData(model, "contact");
        return "website/contact";
    }

    @GetMapping("/portfolio")
    public String portfolio(Model model) {
        addPageData(model, "portfolio");
        return "website/portfolio";
    }

    @GetMapping("/used-cars")
    public String usedCars(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "") String brand,
                           @RequestParam(defaultValue = "") String fuel,
                           @RequestParam(defaultValue = "") String condition,
                           Model model) {
        var allUsed = vehicleService.getUsedCars();

        // Apply filters
        var filtered = allUsed.stream()
            .filter(v -> brand.isBlank() || brand.equalsIgnoreCase(v.getBrand()))
            .filter(v -> fuel.isBlank() || fuel.equalsIgnoreCase(v.getFuelType()))
            .filter(v -> condition.isBlank() || condition.equalsIgnoreCase(v.getCondition()))
            .filter(v -> search.isBlank() ||
                (v.getName() != null && v.getName().toLowerCase().contains(search.toLowerCase())) ||
                (v.getBrand() != null && v.getBrand().toLowerCase().contains(search.toLowerCase())) ||
                (v.getModel() != null && v.getModel().toLowerCase().contains(search.toLowerCase())))
            .toList();

        // Distinct filter values from all used cars
        var brands = allUsed.stream().map(v -> v.getBrand()).filter(b -> b != null && !b.isBlank()).distinct().sorted().toList();
        var fuels = allUsed.stream().map(v -> v.getFuelType()).filter(f -> f != null && !f.isBlank()).distinct().sorted().toList();
        var conditions = allUsed.stream().map(v -> v.getCondition()).filter(c -> c != null && !c.isBlank()).distinct().toList();

        model.addAttribute("vehicles", filtered);
        model.addAttribute("brands", brands);
        model.addAttribute("fuels", fuels);
        model.addAttribute("conditions", conditions);
        model.addAttribute("search", search);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedFuel", fuel);
        model.addAttribute("selectedCondition", condition);
        model.addAttribute("totalCount", filtered.size());
        return "website/used-cars";
    }
}
