package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByCreatedAtDesc();

    List<Recipe> findByFamilyIdOrderByCreatedAtDesc(Long familyId);
}
