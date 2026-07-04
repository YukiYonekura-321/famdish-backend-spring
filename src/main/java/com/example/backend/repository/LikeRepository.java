package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {

    List<Like> findByMemberFamilyId(Long familyId);
}
