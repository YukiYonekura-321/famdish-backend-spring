package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByFamilyIdOrderByName(Long familyId);

    Optional<Stock> findByIdAndFamilyId(Long id, Long familyId);

    List<Stock> findByFamilyId(Long familyId);
}

