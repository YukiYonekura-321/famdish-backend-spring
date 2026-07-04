package com.example.backend.controller;

import java.util.List;

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

import com.example.backend.dto.MenuIdResponse;
import com.example.backend.dto.MenuResponse;
import com.example.backend.dto.MenuUpsertRequest;
import com.example.backend.service.MenuService;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // GET /api/menus
    @GetMapping
    public ResponseEntity<List<MenuResponse>> index() {
        return ResponseEntity.ok(menuService.index());
    }

    // POST /api/menus
    @PostMapping
    public ResponseEntity<MenuIdResponse> create(@RequestBody MenuUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(request.menu()));
    }

    // PATCH /api/menus/:id
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody MenuUpsertRequest request) {
        menuService.update(id, request.menu());
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/menus/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        menuService.destroy(id);
        return ResponseEntity.noContent().build();
    }
}
