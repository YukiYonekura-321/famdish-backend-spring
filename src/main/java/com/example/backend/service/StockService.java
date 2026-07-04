package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.StockAttributesRequest;
import com.example.backend.dto.StockResponse;
import com.example.backend.entity.Family;
import com.example.backend.entity.Stock;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.NotFoundException;
import com.example.backend.exception.UnprocessableEntityException;
import com.example.backend.repository.StockRepository;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final UserService userService;

    public StockService(StockRepository stockRepository, UserService userService) {
        this.stockRepository = stockRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> index() {
        Family family = currentFamily();
        return stockRepository.findByFamilyIdOrderByName(family.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StockResponse create(StockAttributesRequest attrs) {
        Family family = currentFamily();

        if (attrs == null || attrs.name() == null || attrs.name().isBlank()) {
            throw new UnprocessableEntityException("Name can't be blank");
        }

        Stock stock = new Stock();
        stock.setFamily(family);
        stock.setName(attrs.name());
        stock.setQuantity(attrs.quantity());
        stock.setUnit(attrs.unit());
        stock = stockRepository.save(stock);

        return toResponse(stock);
    }

    @Transactional
    public StockResponse update(Long id, StockAttributesRequest attrs) {
        Stock stock = findOwnStock(id);

        if (attrs != null) {
            if (attrs.name() != null) {
                if (attrs.name().isBlank()) {
                    throw new UnprocessableEntityException("Name can't be blank");
                }
                stock.setName(attrs.name());
            }
            if (attrs.quantity() != null) {
                stock.setQuantity(attrs.quantity());
            }
            if (attrs.unit() != null) {
                stock.setUnit(attrs.unit());
            }
        }
        stock = stockRepository.save(stock);

        return toResponse(stock);
    }

    @Transactional
    public void destroy(Long id) {
        Stock stock = findOwnStock(id);
        stockRepository.delete(stock);
    }

    private Stock findOwnStock(Long id) {
        Family family = currentFamily();
        return stockRepository.findByIdAndFamilyId(id, family.getId())
                .orElseThrow(() -> new NotFoundException("在庫が見つかりません"));
    }

    private Family currentFamily() {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        if (family == null) {
            throw new BadRequestException("家族が見つかりません");
        }
        return family;
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getName(),
                stock.getQuantity() != null ? stock.getQuantity().doubleValue() : null,
                stock.getUnit()
        );
    }
}
