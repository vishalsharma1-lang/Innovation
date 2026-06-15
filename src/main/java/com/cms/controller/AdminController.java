package com.cms.controller;

import com.cms.entity.*;
import com.cms.repository.*;
import com.cms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private SeoService seoService;
    @Autowired private ContentService contentService;
    @Autowired private ImageService imageService;
    @Autowired private BannerService bannerService;

    // ─── Login ───────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password.");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully.");
        return "admin/login";
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalSeo", seoService.getAllSeoSettings().size());
        model.addAttribute("totalContent", contentService.getAllContent().size());
        model.addAttribute("totalImages", imageService.getAllImages().size());
        model.addAttribute("totalBanners", bannerService.getAllBanners().size());
        model.addAttribute("recentContent", contentService.getContentPaginated(
                PageRequest.of(0, 5, Sort.by("updatedAt").descending())).getContent());
        model.addAttribute("recentBanners", bannerService.getBannersPaginated(
                PageRequest.of(0, 5, Sort.by("updatedAt").descending())).getContent());
        return "admin/dashboard";
    }

    // ─── SEO Management ──────────────────────────────────────────────────────

    @GetMapping("/seo")
    public String seoList(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(defaultValue = "") String search,
                          Model model) {
        Page<SeoSettings> seoPage = seoService.searchSeoSettings(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        model.addAttribute("seoPage", seoPage);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        return "admin/seo/list";
    }

    @GetMapping("/seo/new")
    public String newSeoForm(Model model) {
        model.addAttribute("seo", new SeoSettings());
        model.addAttribute("isNew", true);
        return "admin/seo/form";
    }

    @GetMapping("/seo/edit/{id}")
    public String editSeoForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return seoService.getSeoById(id).map(seo -> {
            model.addAttribute("seo", seo);
            model.addAttribute("isNew", false);
            return "admin/seo/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "SEO record not found.");
            return "redirect:/admin/seo";
        });
    }

    @PostMapping("/seo/save")
    public String saveSeo(@ModelAttribute SeoSettings seoSettings, RedirectAttributes ra) {
        try {
            seoService.createOrUpdateSeo(seoSettings);
            ra.addFlashAttribute("success", "SEO settings saved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving SEO settings: " + e.getMessage());
        }
        return "redirect:/admin/seo";
    }

    @PostMapping("/seo/delete/{id}")
    public String deleteSeo(@PathVariable Long id, RedirectAttributes ra) {
        try {
            seoService.deleteSeoSettings(id);
            ra.addFlashAttribute("success", "SEO record deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting SEO record: " + e.getMessage());
        }
        return "redirect:/admin/seo";
    }

    // ─── Content Management ──────────────────────────────────────────────────

    @GetMapping("/content")
    public String contentList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "") String search,
                              Model model) {
        Page<ContentSettings> contentPage = contentService.searchContent(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        model.addAttribute("contentPage", contentPage);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("images", imageService.getActiveImages());
        return "admin/content/list";
    }

    @GetMapping("/content/new")
    public String newContentForm(Model model) {
        model.addAttribute("content", new ContentSettings());
        model.addAttribute("isNew", true);
        model.addAttribute("images", imageService.getActiveImages());
        return "admin/content/form";
    }

    @GetMapping("/content/edit/{id}")
    public String editContentForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return contentService.getContentById(id).map(content -> {
            model.addAttribute("content", content);
            model.addAttribute("isNew", false);
            model.addAttribute("images", imageService.getActiveImages());
            return "admin/content/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Content record not found.");
            return "redirect:/admin/content";
        });
    }

    @PostMapping("/content/save")
    public String saveContent(@ModelAttribute ContentSettings contentSettings, RedirectAttributes ra) {
        try {
            contentService.createOrUpdateContent(contentSettings);
            ra.addFlashAttribute("success", "Content saved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving content: " + e.getMessage());
        }
        return "redirect:/admin/content";
    }

    @PostMapping("/content/delete/{id}")
    public String deleteContent(@PathVariable Long id, RedirectAttributes ra) {
        try {
            contentService.deleteContent(id);
            ra.addFlashAttribute("success", "Content deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting content: " + e.getMessage());
        }
        return "redirect:/admin/content";
    }

    // ─── Banner Management ───────────────────────────────────────────────────

    @GetMapping("/banners")
    public String bannerList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "") String search,
                             Model model) {
        Page<BannerSettings> bannerPage = bannerService.searchBanners(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        model.addAttribute("bannerPage", bannerPage);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("images", imageService.getActiveImages());
        return "admin/banners/list";
    }

    @GetMapping("/banners/new")
    public String newBannerForm(Model model) {
        model.addAttribute("banner", new BannerSettings());
        model.addAttribute("isNew", true);
        model.addAttribute("images", imageService.getActiveImages());
        return "admin/banners/form";
    }

    @GetMapping("/banners/edit/{id}")
    public String editBannerForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return bannerService.getBannerById(id).map(banner -> {
            model.addAttribute("banner", banner);
            model.addAttribute("isNew", false);
            model.addAttribute("images", imageService.getActiveImages());
            return "admin/banners/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Banner not found.");
            return "redirect:/admin/banners";
        });
    }

    @PostMapping("/banners/save")
    public String saveBanner(@ModelAttribute BannerSettings bannerSettings, RedirectAttributes ra) {
        try {
            bannerService.saveBanner(bannerSettings);
            ra.addFlashAttribute("success", "Banner saved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving banner: " + e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @PostMapping("/banners/delete/{id}")
    public String deleteBanner(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bannerService.deleteBanner(id);
            ra.addFlashAttribute("success", "Banner deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting banner: " + e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    // ─── Settings ────────────────────────────────────────────────────────────

    @GetMapping("/settings")
    public String settings(Model model) {
        return "admin/settings";
    }

    // ─── Dealer Management ───────────────────────────────────────────────────
    // Handled by AutoAdminController → /admin/dealers

    // ─── Deal Management ─────────────────────────────────────────────────────

    @GetMapping("/deals")
    public String dealList() {
        return "admin/deals/list";
    }
}
