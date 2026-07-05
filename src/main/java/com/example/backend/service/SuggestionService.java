package com.example.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.SuggestionCreateRequest;
import com.example.backend.dto.SuggestionCreateResponse;
import com.example.backend.entity.Family;
import com.example.backend.entity.Member;
import com.example.backend.entity.Suggestion;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.NotFoundException;
import com.example.backend.repository.SuggestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final UserService userService;
    private final SuggestionGenerateService suggestionGenerateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SuggestionService(SuggestionRepository suggestionRepository, UserService userService,
                              SuggestionGenerateService suggestionGenerateService) {
        this.suggestionRepository = suggestionRepository;
        this.userService = userService;
        this.suggestionGenerateService = suggestionGenerateService;
    }

    @Transactional
    public SuggestionCreateResponse create(SuggestionCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        if (family == null) {
            throw new BadRequestException("家族が見つかりません");
        }

        Member currentMember = currentUser.getMember();
        if (currentMember == null) {
            throw new BadRequestException("メンバーが見つかりません");
        }
        if (family.getTodayCookId() == null || !family.getTodayCookId().equals(currentMember.getId())) {
            throw new ForbiddenException("今日の料理担当者ではありません");
        }

        int days = (request.days() != null && request.days() > 0) ? request.days() : 1;

        ObjectNode constraints = objectMapper.createObjectNode();
        if (request.budget() != null && !request.budget().toString().isBlank()) {
            constraints.put("budget", request.budget().toString());
        }
        if (request.cookingTime() != null) {
            constraints.put("cooking_time", request.cookingTime());
        }

        String feedbackJson = fetchFeedback(request.sgId());

        Suggestion suggestion = new Suggestion();
        suggestion.setFamily(family);
        suggestion.setRequests(constraints);
        suggestion.setProposer(currentMember);
        suggestion.setStatus(Suggestion.PENDING);
        suggestion.setImageUrl(null);
        suggestion = suggestionRepository.save(suggestion);

        suggestionGenerateService.generate(suggestion.getId(), family.getId(), feedbackJson, constraints, days);

        return new SuggestionCreateResponse(suggestion.getId(), suggestion.getStatus());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> show(Long id) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", suggestion.getId());
        body.put("status", suggestion.getStatus());

        if (suggestion.isCompleted()) {
            body.put("suggest_field", parseJsonOrNull(suggestion.getAiRawJson()));
            body.put("image_url", suggestion.getImageUrl());
        } else if (suggestion.isFailed()) {
            body.put("error", "AI生成に失敗しました");
        }
        return body;
    }

    @Transactional
    public void feedback(Long id, String chosenOption, String feedbackNote) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        suggestion.setChosenOption(chosenOption);
        suggestion.setFeedback(feedbackNote);
        suggestionRepository.save(suggestion);
    }

    private String fetchFeedback(Long sgId) {
        try {
            if (sgId != null) {
                String feedback = suggestionRepository.findById(sgId)
                        .map(Suggestion::getFeedback)
                        .orElse(null);
                return objectMapper.writeValueAsString(feedback);
            }
            return "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    private JsonNode parseJsonOrNull(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(rawJson);
        } catch (Exception e) {
            return null;
        }
    }
}
