package com.example.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.InvitationAcceptResponse;
import com.example.backend.dto.InvitationCreateResponse;
import com.example.backend.dto.InvitationShowResponse;
import com.example.backend.entity.Family;
import com.example.backend.entity.Invitation;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.NotFoundException;
import com.example.backend.exception.UnprocessableEntityException;
import com.example.backend.repository.InvitationRepository;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public InvitationService(InvitationRepository invitationRepository, UserService userService) {
        this.invitationRepository = invitationRepository;
        this.userService = userService;
    }

    @Transactional
    public InvitationCreateResponse create() {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        if (family == null) {
            throw new BadRequestException("家族が見つかりません");
        }

        Invitation invitation = new Invitation();
        invitation.setFamily(family);
        invitation.setToken(generateToken());
        invitation.setUsed(false);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation = invitationRepository.save(invitation);

        String inviteUrl = frontendUrl + "/invite/" + invitation.getToken();
        return new InvitationCreateResponse(invitation.getToken(), inviteUrl, invitation.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public InvitationShowResponse show(String token) {
        Invitation invitation = findValidInvitation(token);
        return new InvitationShowResponse(true, invitation.getFamily().getName());
    }

    @Transactional
    public InvitationAcceptResponse accept(String token) {
        User currentUser = userService.getCurrentUser();
        Invitation invitation = findValidInvitation(token);

        if (currentUser.getFamilyId() != null) {
            throw new UnprocessableEntityException("既に家族に所属しています");
        }

        currentUser.setFamily(invitation.getFamily());
        invitation.setUsed(true);
        invitationRepository.save(invitation);

        return new InvitationAcceptResponse("招待を受諾しました", invitation.getFamily().getId(), invitation.getFamily().getName());
    }

    private Invitation findValidInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("招待が見つかりません"));
        if (!invitation.isValidInvitation()) {
            throw new UnprocessableEntityException("招待が無効または期限切れです");
        }
        return invitation;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
