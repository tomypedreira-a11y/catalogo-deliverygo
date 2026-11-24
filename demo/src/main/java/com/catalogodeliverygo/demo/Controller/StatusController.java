package com.catalogodeliverygo.demo.Controller;

import io.swagger.v3.oas.annotations.Hidden;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class StatusController {

    @GetMapping("/api/v1/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
