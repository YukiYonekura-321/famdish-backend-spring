package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.IdNameResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.LikeRepository;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;

    public LikeService(LikeRepository likeRepository, UserService userService) {
        this.likeRepository = likeRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<IdNameResponse> index() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getFamilyId() == null) {
            return List.of();
        }

        return likeRepository.findByMemberFamilyId(currentUser.getFamilyId()).stream()
                .map(like -> new IdNameResponse(like.getId(), like.getName()))
                .toList();
    }
}
