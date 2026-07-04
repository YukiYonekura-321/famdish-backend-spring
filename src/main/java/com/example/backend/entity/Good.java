package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rails 側は menu_id / suggestion_id のどちらか一方が入る「いいね」レコード。
 * DB上は user_id, menu_id が integer, suggestion_id が bigint (FK制約なし)。
 */
@Entity
@Table(name = "goods")
@Getter
@Setter
@NoArgsConstructor
public class Good extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "menu_id")
    private Integer menuId;

    @Column(name = "suggestion_id")
    private Long suggestionId;

    public Good(Integer userId, Integer menuId, Long suggestionId) {
        this.userId = userId;
        this.menuId = menuId;
        this.suggestionId = suggestionId;
    }
}

