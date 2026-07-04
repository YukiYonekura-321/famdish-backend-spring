package com.example.backend.service;

import java.util.LinkedHashMap;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.GoodCheckResponse;
import com.example.backend.dto.GoodIdResponse;
import com.example.backend.entity.Good;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.GoodRepository;

@Service
public class GoodService {

    private final GoodRepository goodRepository;
    private final UserService userService;

    public GoodService(GoodRepository goodRepository, UserService userService) {
        this.goodRepository = goodRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public GoodCheckResponse checkByMenuId(Long menuId) {
        if (menuId == null) {
            throw new BadRequestException("menu_id が必要です");
        }
        Integer currentUserId = currentUserId();
        Good good = goodRepository.findByUserIdAndMenuId(currentUserId, menuId.intValue()).orElse(null);
        return new GoodCheckResponse(good != null, good != null ? new GoodIdResponse(good.getId()) : null);
    }

    @Transactional(readOnly = true)
    public GoodCheckResponse checkBySuggestionId(Long suggestionId) {
        if (suggestionId == null) {
            throw new BadRequestException("suggestion_id が必要です");
        }
        Integer currentUserId = currentUserId();
        Good good = goodRepository.findByUserIdAndSuggestionId(currentUserId, suggestionId).orElse(null);
        return new GoodCheckResponse(good != null, good != null ? new GoodIdResponse(good.getId()) : null);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> countByMenuId(Long menuId) {
        if (menuId == null) {
            throw new BadRequestException("menu_id が必要です");
        }
        long count = goodRepository.countByMenuId(menuId.intValue());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("menu_id", menuId);
        body.put("count", count);
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> countBySuggestionId(Long suggestionId) {
        if (suggestionId == null) {
            throw new BadRequestException("suggestion_id が必要です");
        }
        long count = goodRepository.countBySuggestionId(suggestionId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("suggestion_id", suggestionId);
        body.put("count", count);
        return body;
    }

    @Transactional
    public GoodIdResponse createByMenuId(Long menuId) {
        if (menuId == null) {
            throw new BadRequestException("menu_id が必要です");
        }
        Integer currentUserId = currentUserId();

        Good existing = goodRepository.findByUserIdAndMenuId(currentUserId, menuId.intValue()).orElse(null);
        if (existing != null) {
            return new GoodIdResponse(existing.getId());
        }

        Good good = new Good(currentUserId, menuId.intValue(), null);
        good = goodRepository.save(good);
        return new GoodIdResponse(good.getId());
    }

    @Transactional
    public GoodIdResponse createBySuggestionId(Long suggestionId) {
        if (suggestionId == null) {
            throw new BadRequestException("suggestion_id が必要です");
        }
        Integer currentUserId = currentUserId();

        Good existing = goodRepository.findByUserIdAndSuggestionId(currentUserId, suggestionId).orElse(null);
        if (existing != null) {
            return new GoodIdResponse(existing.getId());
        }

        Good good = new Good(currentUserId, null, suggestionId);
        good = goodRepository.save(good);
        return new GoodIdResponse(good.getId());
    }

    @Transactional
    public void destroy(Long id) {
        Integer currentUserId = currentUserId();
        Good good = goodRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new UnauthorizedException("権限がありません"));
        goodRepository.delete(good);
    }

    private Integer currentUserId() {
        User currentUser = userService.getCurrentUser();
        return currentUser.getId().intValue();
    }
}
