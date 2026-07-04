package com.example.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "families")
@Getter
@Setter
@NoArgsConstructor
public class Family extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    // belongs_to :today_cook, class_name: "Member", optional: true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "today_cook_id")
    private Member todayCook;

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<Stock> stocks = new ArrayList<>();

    @OneToMany(mappedBy = "family", fetch = FetchType.LAZY)
    private List<Invitation> invitations = new ArrayList<>();

    public Family(String name) {
        this.name = name;
    }

    public Long getTodayCookId() {
        return todayCook != null ? todayCook.getId() : null;
    }
}
