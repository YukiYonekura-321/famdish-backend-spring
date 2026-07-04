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

@Entity
@Table(name = "trial_usages")
@Getter
@Setter
@NoArgsConstructor
public class TrialUsage extends BaseEntity {

    public static final int TRIAL_LIMIT = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    public TrialUsage(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public boolean isLimitExceeded() {
        return usageCount >= TRIAL_LIMIT;
    }

    public void increment() {
        this.usageCount += 1;
    }
}
