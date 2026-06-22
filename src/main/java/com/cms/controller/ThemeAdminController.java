package com.cms.controller;

import com.cms.entity.Theme;
import com.cms.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/themes")
@RequiredArgsConstructor
public class ThemeAdminController {

    private final ThemeService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("themes", service.findAll());
        model.addAttribute("activeTheme", service.findActive().orElse(null));
        model.addAttribute("currentUri", "/admin/themes");
        return "admin/themes/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("theme", new Theme());
        model.addAttribute("currentUri", "/admin/themes");
        return "admin/themes/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Theme theme = service.findById(id).orElseThrow();
        model.addAttribute("theme", theme);
        model.addAttribute("currentUri", "/admin/themes");
        return "admin/themes/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Theme theme, RedirectAttributes ra) {
        service.save(theme);
        ra.addFlashAttribute("success", "Theme '" + theme.getName() + "' saved successfully.");
        return "redirect:/admin/themes";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        Theme t = service.activate(id);
        ra.addFlashAttribute("success", "Theme '" + t.getName() + "' is now active on the website.");
        return "redirect:/admin/themes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Theme deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/themes";
    }
}
