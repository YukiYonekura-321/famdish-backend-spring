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

import com.example.backend.dto.StockResponse;
import com.example.backend.dto.StockUpsertRequest;
import com.example.backend.service.StockService;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // GET /api/stocks
    @GetMapping
    public ResponseEntity<List<StockResponse>> index() {
        return ResponseEntity.ok(stockService.index());
    }

    // POST /api/stocks
    @PostMapping
    public ResponseEntity<StockResponse> create(@RequestBody StockUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.create(request.stock()));
    }

    // PATCH /api/stocks/:id
    @PatchMapping("/{id}")
    public ResponseEntity<StockResponse> update(@PathVariable Long id, @RequestBody StockUpsertRequest request) {
        return ResponseEntity.ok(stockService.update(id, request.stock()));
    }

    // DELETE /api/stocks/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        stockService.destroy(id);
        return ResponseEntity.noContent().build();
    }
}
