package com.cms.controller;

import com.cms.entity.page.*;
import com.cms.service.page.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pages")
public class PageAdminController {

    @Autowired private PageService pageService;

    @GetMapping
    public String pageList(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "") String search,
                           Model model) {
        Page<DynamicPage> pages = pageService.searchPages(
                search, PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        model.addAttribute("pages", pages);
        model.addAttribute("search", search);
        return "admin/pages/list";
    }

    @GetMapping("/new")
    public String newPage(Model model) {
        model.addAttribute("dynamicPage", new DynamicPage());
        model.addAttribute("isNew", true);
        return "admin/pages/form";
    }

    @GetMapping("/replicate")
    public String replicateFromUrl() {
        return "admin/pages/replicate";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return pageService.getById(id).map(pg -> {
            model.addAttribute("dynamicPage", pg);
            model.addAttribute("isNew", false);
            model.addAttribute("sections", pageService.getSections(id));
            model.addAttribute("versions", pageService.getVersionHistory(id));
            return "admin/pages/form";
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Page not found"); return "redirect:/admin/pages"; });
    }

    @PostMapping("/save")
    public String savePage(@ModelAttribute DynamicPage pg, RedirectAttributes ra) {
        try {
            pageService.savePage(pg);
            ra.addFlashAttribute("success", "Page saved!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/pages";
    }

    @PostMapping("/delete/{id}")
    public String deletePage(@PathVariable Long id, RedirectAttributes ra) {
        pageService.deletePage(id);
        ra.addFlashAttribute("success", "Page deleted.");
        return "redirect:/admin/pages";
    }

    @PostMapping("/publish/{id}")
    public String publishPage(@PathVariable Long id, RedirectAttributes ra) {
        pageService.publishPage(id);
        ra.addFlashAttribute("success", "Page published!");
        return "redirect:/admin/pages/edit/" + id;
    }

    @PostMapping("/unpublish/{id}")
    public String unpublishPage(@PathVariable Long id, RedirectAttributes ra) {
        pageService.unpublishPage(id);
        ra.addFlashAttribute("success", "Page unpublished.");
        return "redirect:/admin/pages/edit/" + id;
    }
}
