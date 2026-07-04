package com.example.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.MessageResponse;
import com.example.backend.dto.RecipeCreateRequest;
import com.example.backend.dto.RecipeCreateResponse;
import com.example.backend.dto.RecipeDetailResponse;
import com.example.backend.dto.RecipeExplainRequest;
import com.example.backend.dto.RecipeListItemResponse;
import com.example.backend.dto.RecipeUpdateRequest;
import com.example.backend.service.RecipeService;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // POST /api/recipes/explain
    @PostMapping("/explain")
    public ResponseEntity<Map<String, Object>> explain(@RequestBody RecipeExplainRequest request) {
        return ResponseEntity.ok(recipeService.explain(request.dishName(), request.servings(), request.suggestionId()));
    }

    // GET /api/recipes
    @GetMapping
    public ResponseEntity<List<RecipeListItemResponse>> index() {
        return ResponseEntity.ok(recipeService.index());
    }

    // GET /api/recipes/family
    @GetMapping("/family")
    public ResponseEntity<List<RecipeListItemResponse>> family() {
        return ResponseEntity.ok(recipeService.family());
    }

    // POST /api/recipes
    @PostMapping
    public ResponseEntity<RecipeCreateResponse> create(@RequestBody RecipeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(request));
    }

    // GET /api/recipes/:id
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> show(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.show(id));
    }

    // PATCH /api/recipes/:id
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody RecipeUpdateRequest request) {
        recipeService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/recipes/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> destroy(@PathVariable Long id) {
        String message = recipeService.destroy(id);
        return ResponseEntity.ok(new MessageResponse(message));
    }
}
