package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Family;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    List<Family> findAllByOrderByName();
}
