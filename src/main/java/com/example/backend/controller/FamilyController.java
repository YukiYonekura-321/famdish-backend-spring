package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AssignCookRequest;
import com.example.backend.dto.AssignCookResponse;
import com.example.backend.dto.FamilyResponse;
import com.example.backend.service.FamilyService;

@RestController
@RequestMapping("/api/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    // GET /api/families
    @GetMapping
    public ResponseEntity<FamilyResponse> index() {
        return ResponseEntity.ok(familyService.getMyFamily());
    }

    // POST /api/families/assign_cook
    @PostMapping("/assign_cook")
    public ResponseEntity<AssignCookResponse> assignCook(@RequestBody AssignCookRequest request) {
        return ResponseEntity.ok(familyService.assignCook(request.memberId()));
    }
}

