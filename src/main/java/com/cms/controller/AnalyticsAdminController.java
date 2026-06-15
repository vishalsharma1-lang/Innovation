package com.cms.controller;

import com.cms.service.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/analytics")
public class AnalyticsAdminController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping({"", "/", "/overview"})
    public String overview(Model model, HttpServletRequest request) {
        model.addAttribute("stats", analyticsService.getOverviewStats());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/analytics/overview";
    }

    @GetMapping("/deals")
    public String deals(Model model, HttpServletRequest request) {
        model.addAttribute("data", analyticsService.getDealAnalytics());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/analytics/deals";
    }

    @GetMapping("/vehicles")
    public String vehicles(Model model, HttpServletRequest request) {
        model.addAttribute("data", analyticsService.getVehicleAnalytics());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/analytics/vehicles";
    }

    @GetMapping("/funnel")
    public String funnel(Model model, HttpServletRequest request) {
        model.addAttribute("data", analyticsService.getFunnelAnalytics());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/analytics/funnel";
    }

    @GetMapping("/search")
    public String search(Model model, HttpServletRequest request) {
        model.addAttribute("data", analyticsService.getSearchAnalytics());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/analytics/search";
    }
}
