package com.example.backend.security;

public class SecurityUtil {

    public static String getFirebaseUid() {

//        return SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
        return "test-firebase-uid";
    }
}