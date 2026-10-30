package com.h_group.gvs_terceirizacoes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "message", "GVS Terceirizacoes API is running",
                "docs", "/swagger-ui.html"
        );
    }
}
