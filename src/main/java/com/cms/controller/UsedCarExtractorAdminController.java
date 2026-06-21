package com.cms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/used-car")
public class UsedCarExtractorAdminController {

    @GetMapping("/extractor")
    public String extractorPage(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/used-car/extractor";
    }
}
