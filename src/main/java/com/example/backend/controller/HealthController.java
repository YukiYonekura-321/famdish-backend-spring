package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // GET /api/health
    @GetMapping("/api/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}
