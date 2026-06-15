package com.cms.controller;

import com.cms.entity.page.*;
import com.cms.service.page.PageService;
import com.cms.service.page.YouTubeReelsService;
import com.cms.service.vehicle.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Renders dynamic pages on the public website.
 * URL: /page/{slug}
 */
@Controller
public class DynamicPageWebController {

    @Autowired private PageService pageService;
    @Autowired private VehicleService vehicleService;
    @Autowired private YouTubeReelsService youtubeService;

    @GetMapping("/page")
    public String pageIndex() {
        return "redirect:/vehicles";
    }

    @GetMapping("/page/{slug}")
    public String renderDynamicPage(@PathVariable String slug, Model model) {
        return pageService.getPublishedBySlug(slug).map(page -> {
            List<PageSection> sections = pageService.getVisibleSections(page.getId());
            model.addAttribute("page", page);
            model.addAttribute("sections", sections);

            // Always include vehicle data for dynamic pages
            model.addAttribute("vehicles", vehicleService.getActiveVehicles());
            model.addAttribute("featuredVehicles", vehicleService.getFeaturedVehicles());

            // YouTube Reels (if enabled for this page)
            if (Boolean.TRUE.equals(page.getYoutubeEnabled())) {
                model.addAttribute("youtubeVideos", youtubeService.getVideosForPage(page));
                model.addAttribute("youtubeLayout", page.getYoutubeLayout() != null ? page.getYoutubeLayout() : "grid");
                model.addAttribute("youtubeAutoplay", page.getYoutubeAutoplay());
                model.addAttribute("youtubeShowTitle", page.getYoutubeShowTitle());
                model.addAttribute("youtubeShowDesc", page.getYoutubeShowDesc());
            }

            return "website/dynamic-page";
        }).orElse("redirect:/");
    }
}
