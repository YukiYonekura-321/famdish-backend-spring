package com.example.backend.controller;

import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.GoodCheckResponse;
import com.example.backend.dto.GoodCreateRequest;
import com.example.backend.dto.GoodIdResponse;
import com.example.backend.service.GoodService;

@RestController
@RequestMapping("/api/goods")
public class GoodController {

    private final GoodService goodService;

    public GoodController(GoodService goodService) {
        this.goodService = goodService;
    }

    // GET /api/goods/check?menu_id=123
    @GetMapping("/check")
    public ResponseEntity<GoodCheckResponse> check(@RequestParam(name = "menu_id") Long menuId) {
        return ResponseEntity.ok(goodService.checkByMenuId(menuId));
    }

    // GET /api/goods/count?menu_id=1
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count(@RequestParam(name = "menu_id") Long menuId) {
        return ResponseEntity.ok(goodService.countByMenuId(menuId));
    }

    // GET /api/goods/check_suggestion?suggestion_id=456
    @GetMapping("/check_suggestion")
    public ResponseEntity<GoodCheckResponse> checkSuggestion(@RequestParam(name = "suggestion_id") Long suggestionId) {
        return ResponseEntity.ok(goodService.checkBySuggestionId(suggestionId));
    }

    // GET /api/goods/count_suggestion?suggestion_id=1
    @GetMapping("/count_suggestion")
    public ResponseEntity<Map<String, Object>> countSuggestion(@RequestParam(name = "suggestion_id") Long suggestionId) {
        return ResponseEntity.ok(goodService.countBySuggestionId(suggestionId));
    }

    // POST /api/goods
    @PostMapping
    public ResponseEntity<GoodIdResponse> create(@RequestBody GoodCreateRequest request) {
        GoodIdResponse existingOrNew = goodService.createByMenuId(request.resolveMenuId());
        return ResponseEntity.status(HttpStatus.CREATED).body(existingOrNew);
    }

    // POST /api/goods/create_suggestion
    @PostMapping("/create_suggestion")
    public ResponseEntity<GoodIdResponse> createSuggestion(@RequestBody GoodCreateRequest request) {
        GoodIdResponse existingOrNew = goodService.createBySuggestionId(request.resolveSuggestionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(existingOrNew);
    }

    // DELETE /api/goods/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        goodService.destroy(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/goods/:id/destroy_suggestion
    @DeleteMapping("/{id}/destroy_suggestion")
    public ResponseEntity<Void> destroySuggestion(@PathVariable Long id) {
        goodService.destroy(id);
        return ResponseEntity.noContent().build();
    }
}

