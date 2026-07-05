package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Suggestion;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
}
