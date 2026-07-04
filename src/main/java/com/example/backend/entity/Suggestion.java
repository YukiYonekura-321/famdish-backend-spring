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
@Table(name = "suggestions")
@Getter
@Setter
@NoArgsConstructor
public class Suggestion extends BaseEntity {

    public static final String PENDING = "pending";
    public static final String PROCESSING = "processing";
    public static final String COMPLETED = "completed";
    public static final String FAILED = "failed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // belongs_to :family, optional: true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "requests", columnDefinition = "json")
    private JsonNode requests;

    @Column(name = "ai_raw_json", columnDefinition = "text")
    private String aiRawJson;

    @Column(name = "chosen_option")
    private String chosenOption;

    @Column(name = "feedback", columnDefinition = "text")
    private String feedback;

    // belongs_to :member, foreign_key: :proposer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer", nullable = false)
    private Member proposer;

    @Column(name = "status", nullable = false)
    private String status = PENDING;

    @Column(name = "image_url")
    private String imageUrl;

    public boolean isCompleted() {
        return COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return FAILED.equals(status);
    }

    public boolean isPending() {
        return PENDING.equals(status);
    }
}

