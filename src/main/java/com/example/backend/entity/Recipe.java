package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.example.backend.entity.converter.JsonNodeConverter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
public class Recipe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dish_name", nullable = false)
    private String dishName;

    // belongs_to :member, foreign_key: :proposer, optional: true (単純な参照として保持)
    @Column(name = "proposer")
    private Long proposer;

    @Column(name = "servings")
    private Integer servings;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "missing_ingredients", columnDefinition = "json")
    private JsonNode missingIngredients;

    @Column(name = "cooking_time")
    private Integer cookingTime;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "steps", columnDefinition = "json")
    private JsonNode steps;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    // belongs_to :family, optional: true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    // belongs_to :suggestion, optional: true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion_id")
    private Suggestion suggestion;

    @Column(name = "image_url")
    private String imageUrl;
}

