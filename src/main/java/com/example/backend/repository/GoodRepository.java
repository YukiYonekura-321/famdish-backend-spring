package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Good;

public interface GoodRepository extends JpaRepository<Good, Long> {

    Optional<Good> findByUserIdAndMenuId(Integer userId, Integer menuId);

    Optional<Good> findByUserIdAndSuggestionId(Integer userId, Long suggestionId);

    Optional<Good> findByIdAndUserId(Long id, Integer userId);

    long countByMenuId(Integer menuId);

    long countBySuggestionId(Long suggestionId);
}
