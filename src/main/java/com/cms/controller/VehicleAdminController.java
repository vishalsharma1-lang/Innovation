package com.cms.controller;

import com.cms.entity.vehicle.*;
import com.cms.service.vehicle.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vehicles")
public class VehicleAdminController {

    @Autowired private VehicleService vehicleService;

    // ─── Vehicle List ──────────────────────────────────────

    @GetMapping
    public String vehicleList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "ALL") String typeFilter,
                              Model model) {
        Page<Vehicle> vehiclePage;
        if ("ALL".equals(typeFilter) || typeFilter.isBlank()) {
            vehiclePage = vehicleService.searchVehicles(
                    search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            vehiclePage = vehicleService.searchVehiclesByType(
                    typeFilter, search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        model.addAttribute("vehiclePage", vehiclePage);
        model.addAttribute("search", search);
        model.addAttribute("typeFilter", typeFilter);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalAll", vehicleService.countAll());
        model.addAttribute("totalNew", vehicleService.countByType("NEW"));
        model.addAttribute("totalUsed", vehicleService.countByType("USED"));
        return "admin/vehicles/list";
    }

    // ─── Import From URL ───────────────────────────────────

    @GetMapping("/import")
    public String importFromUrl() {
        return "admin/vehicles/import";
    }

    // ─── Vehicle Create/Edit Form ──────────────────────────

    @GetMapping("/new")
    public String newVehicleForm(@RequestParam(defaultValue = "NEW") String type, Model model) {
        Vehicle v = new Vehicle();
        v.setVehicleType("USED".equals(type) ? "USED" : "NEW");
        model.addAttribute("vehicle", v);
        model.addAttribute("isNew", true);
        return "admin/vehicles/form";
    }

    @GetMapping("/edit/{id}")
    public String editVehicleForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return vehicleService.getById(id).map(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("isNew", false);
            return "admin/vehicles/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Vehicle not found.");
            return "redirect:/admin/vehicles";
        });
    }

    @PostMapping("/save")
    public String saveVehicle(@ModelAttribute Vehicle vehicle, RedirectAttributes ra) {
        try {
            vehicleService.save(vehicle);
            ra.addFlashAttribute("success", "Vehicle saved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving vehicle: " + e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes ra) {
        vehicleService.delete(id);
        ra.addFlashAttribute("success", "Vehicle deleted.");
        return "redirect:/admin/vehicles";
    }

    // ─── Vehicle Detail/Manage Sub-modules ─────────────────

    @GetMapping("/manage/{id}")
    public String manageVehicle(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return vehicleService.getById(id).map(vehicle -> {
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("variants", vehicleService.getVariants(id));
            model.addAttribute("specifications", vehicleService.getSpecs(id));
            model.addAttribute("features", vehicleService.getFeatures(id));
            model.addAttribute("colors", vehicleService.getColors(id));
            model.addAttribute("images", vehicleService.getImages(id));
            model.addAttribute("videos", vehicleService.getVideos(id));
            model.addAttribute("brochures", vehicleService.getBrochures(id));
            model.addAttribute("faqs", vehicleService.getFaqs(id));
            model.addAttribute("modelFaqs", vehicleService.getActiveModelFaqs());
            model.addAttribute("reviews", vehicleService.getReviews(id));
            model.addAttribute("expertReviews", vehicleService.getExpertReviews(id));
            model.addAttribute("news", vehicleService.getNews(id));
            model.addAttribute("offers", vehicleService.getOffers(id));
            model.addAttribute("prices", vehicleService.getPrices(id));
            model.addAttribute("seo", vehicleService.getSeo(id).orElse(new VehicleSeo()));
            return "admin/vehicles/manage";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Vehicle not found.");
            return "redirect:/admin/vehicles";
        });
    }
}
