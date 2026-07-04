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

    /**
     * 現在の Firebase UID に対応する User を取得する。存在しない場合は新規作成する。
     * Rails の ApplicationController#authenticate_user! (User.find_or_create_by) に相当。
     */
    @Transactional
    public User getCurrentUser() {
        String firebaseUid = SecurityUtil.getFirebaseUid();

        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> userRepository.save(new User(firebaseUid)));
    }

    @Transactional
    public void deleteMe() {

        String firebaseUid = SecurityUtil.getFirebaseUid();

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}