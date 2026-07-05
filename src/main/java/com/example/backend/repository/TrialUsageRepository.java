package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.TrialUsage;

public interface TrialUsageRepository extends JpaRepository<TrialUsage, Long> {

    Optional<TrialUsage> findByFirebaseUid(String firebaseUid);
}
