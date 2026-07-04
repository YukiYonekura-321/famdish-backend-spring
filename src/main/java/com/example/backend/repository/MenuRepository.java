package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByMemberFamilyId(Long familyId);

    Optional<Menu> findByIdAndMemberUserId(Long id, Long userId);
}
