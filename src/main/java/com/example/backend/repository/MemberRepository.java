package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserId(Long userId);

    Optional<Member> findByIdAndFamilyId(Long id, Long familyId);

    List<Member> findByFamilyId(Long familyId);
}
