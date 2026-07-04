package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.InvitationAcceptResponse;
import com.example.backend.dto.InvitationCreateResponse;
import com.example.backend.dto.InvitationShowResponse;
import com.example.backend.service.InvitationService;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    // POST /api/invitations
    @PostMapping
    public ResponseEntity<InvitationCreateResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.create());
    }

    // GET /api/invitations/:token
    @GetMapping("/{token}")
    public ResponseEntity<InvitationShowResponse> show(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.show(token));
    }

    // POST /api/invitations/:token/accept
    @PostMapping("/{token}/accept")
    public ResponseEntity<InvitationAcceptResponse> accept(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.accept(token));
    }
}
