package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.TrialAiSuggestionRequest;
import com.example.backend.service.TrialService;

@RestController
@RequestMapping("/api/trial")
public class TrialController {

    private final TrialService trialService;

    public TrialController(TrialService trialService) {
        this.trialService = trialService;
    }

    // POST /api/trial/aisuggestion (認証不要、ヘッダーのトークンからfirebase_uidのみ利用)
    @PostMapping("/aisuggestion")
    public ResponseEntity<Map<String, Object>> aiSuggestion(@RequestBody TrialAiSuggestionRequest request) {
        return trialService.aiSuggestion(request.likes(), request.dislikes());
    }
}
