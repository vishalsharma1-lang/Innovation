package com.cms.controller;

import com.cms.entity.ImageSettings;
import com.cms.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/images")
public class ImageAdminController {

    @Autowired
    private ImageService imageService;

    @GetMapping
    public String imageList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "12") int size,
                            @RequestParam(defaultValue = "") String search,
                            Model model) {
        Page<ImageSettings> imagePage = imageService.searchImages(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("imagePage", imagePage);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        return "admin/images/list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        return "admin/images/upload";
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "altText", required = false) String altText,
                              @RequestParam(value = "category", defaultValue = "general") String category,
                              RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/admin/images/upload";
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            ra.addFlashAttribute("error", "Only image files are allowed.");
            return "redirect:/admin/images/upload";
        }

        try {
            imageService.uploadImage(file, altText, category);
            ra.addFlashAttribute("success", "Image uploaded successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error uploading image: " + e.getMessage());
        }
        return "redirect:/admin/images";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return imageService.getImageById(id).map(image -> {
            model.addAttribute("image", image);
            return "admin/images/edit";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Image not found.");
            return "redirect:/admin/images";
        });
    }

    @PostMapping("/replace/{id}")
    public String replaceImage(@PathVariable Long id,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam(value = "altText", required = false) String altText,
                               @RequestParam(value = "category", required = false) String category,
                               RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a replacement file.");
            return "redirect:/admin/images/edit/" + id;
        }

        try {
            imageService.replaceImage(id, file, altText, category);
            ra.addFlashAttribute("success", "Image replaced successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error replacing image: " + e.getMessage());
        }
        return "redirect:/admin/images";
    }

    @PostMapping("/update/{id}")
    public String updateImageDetails(@PathVariable Long id,
                                     @RequestParam(value = "altText", required = false) String altText,
                                     @RequestParam(value = "category", required = false) String category,
                                     RedirectAttributes ra) {
        try {
            imageService.updateImageDetails(id, altText, category);
            ra.addFlashAttribute("success", "Image details updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating image: " + e.getMessage());
        }
        return "redirect:/admin/images";
    }

    @PostMapping("/delete/{id}")
    public String deleteImage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            imageService.deleteImage(id);
            ra.addFlashAttribute("success", "Image deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting image: " + e.getMessage());
        }
        return "redirect:/admin/images";
    }
}
