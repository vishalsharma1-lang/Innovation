package com.cms.controller;

import com.cms.entity.auto.*;
import com.cms.repository.auto.*;
import com.cms.service.vehicle.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AutoAdminController {

    @Autowired private CarComparisonRepository comparisonRepo;
    @Autowired private LeadCaptureRepository leadRepo;
    @Autowired private VehicleLeadRepository vehicleLeadRepo;
    @Autowired private DealerRepository dealerRepo;
    @Autowired private BlogPostRepository blogRepo;
    @Autowired private VehicleService vehicleService;

    // ─── Comparisons ──────────────────────────────────────
    @GetMapping("/comparisons")
    public String comparisons(Model model) {
        model.addAttribute("comparisons", comparisonRepo.findByIsDeletedFalse(PageRequest.of(0, 50, Sort.by("createdAt").descending())));
        model.addAttribute("vehicles", vehicleService.getActiveVehicles());
        return "admin/auto/comparisons";
    }

    // ─── Leads ────────────────────────────────────────────
    @GetMapping("/leads")
    public String leads(@RequestParam(defaultValue = "") String search, Model model) {
        if (!search.isBlank()) {
            model.addAttribute("leads", vehicleLeadRepo.searchLeads(search, PageRequest.of(0, 50, Sort.by("createdAt").descending())));
        } else {
            model.addAttribute("leads", vehicleLeadRepo.findByIsDeletedFalse(PageRequest.of(0, 50, Sort.by("createdAt").descending())));
        }
        model.addAttribute("search", search);
        return "admin/auto/leads";
    }

    // ─── Dealers ──────────────────────────────────────────
    @GetMapping("/dealers")
    public String dealers(Model model) {
        model.addAttribute("dealers", dealerRepo.findByIsDeletedFalse(PageRequest.of(0, 50, Sort.by("dealerName").ascending())));
        return "admin/dealers/list";
    }

    @PostMapping("/dealers/save")
    public String saveDealer(@ModelAttribute Dealer dealer, RedirectAttributes ra) {
        if (dealer.getId() == null) dealer.setIsDeleted(false);
        dealerRepo.save(dealer);
        ra.addFlashAttribute("success", "Dealer saved!");
        return "redirect:/admin/dealers";
    }

    @PostMapping("/dealers/delete/{id}")
    public String deleteDealer(@PathVariable Long id, RedirectAttributes ra) {
        dealerRepo.findById(id).ifPresent(d -> { d.setIsDeleted(true); dealerRepo.save(d); });
        ra.addFlashAttribute("success", "Dealer deleted.");
        return "redirect:/admin/dealers";
    }

    // ─── Blog ─────────────────────────────────────────────
    @GetMapping("/blog")
    public String blog(@RequestParam(defaultValue = "") String search, Model model) {
        if (!search.isBlank()) {
            model.addAttribute("posts", blogRepo.searchPosts(search, PageRequest.of(0, 50, Sort.by("createdAt").descending())));
        } else {
            model.addAttribute("posts", blogRepo.findByIsDeletedFalse(PageRequest.of(0, 50, Sort.by("createdAt").descending())));
        }
        model.addAttribute("search", search);
        return "admin/auto/blog";
    }

    @PostMapping("/blog/save")
    public String saveBlog(@ModelAttribute BlogPost post, RedirectAttributes ra) {
        if (post.getId() == null) post.setIsDeleted(false);
        if (post.getSlug() == null || post.getSlug().isBlank()) {
            post.setSlug(post.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", ""));
        }
        blogRepo.save(post);
        ra.addFlashAttribute("success", "Post saved!");
        return "redirect:/admin/blog";
    }

    @PostMapping("/blog/delete/{id}")
    public String deleteBlog(@PathVariable Long id, RedirectAttributes ra) {
        blogRepo.findById(id).ifPresent(p -> { p.setIsDeleted(true); blogRepo.save(p); });
        ra.addFlashAttribute("success", "Post deleted.");
        return "redirect:/admin/blog";
    }

    // ─── EMI Calculator ───────────────────────────────────
    @GetMapping("/emi")
    public String emiCalculator() {
        return "admin/auto/emi";
    }
}
