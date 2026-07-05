package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Dislike;

public interface DislikeRepository extends JpaRepository<Dislike, Long> {
}

