package com.example.backend.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.SecurityUtil;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteMe() {

        String firebaseUid = SecurityUtil.getFirebaseUid();

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}