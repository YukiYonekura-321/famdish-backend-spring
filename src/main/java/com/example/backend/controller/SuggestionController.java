package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.SuggestionCreateRequest;
import com.example.backend.dto.SuggestionCreateResponse;
import com.example.backend.dto.SuggestionFeedbackRequest;
import com.example.backend.service.SuggestionService;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    // POST /api/suggestions
    @PostMapping
    public ResponseEntity<SuggestionCreateResponse> create(@RequestBody SuggestionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(suggestionService.create(request));
    }

    // GET /api/suggestions/:id
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> show(@PathVariable Long id) {
        return ResponseEntity.ok(suggestionService.show(id));
    }

    // POST /api/suggestions/:id/feedback
    @PostMapping("/{id}/feedback")
    public ResponseEntity<Void> feedback(@PathVariable Long id, @RequestBody SuggestionFeedbackRequest request) {
        suggestionService.feedback(id, request.chosenOption(), request.feedbackNote());
        return ResponseEntity.noContent().build();
    }
}
