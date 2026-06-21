package com.cms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @Value("${app.autoparts.url:http://localhost:5176}")
    private String autopartsUrl;

    @ModelAttribute("autopartsUrl")
    public String autopartsUrl() {
        return autopartsUrl;
    }
}
