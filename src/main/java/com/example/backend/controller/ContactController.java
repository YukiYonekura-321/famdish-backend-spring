package com.example.backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ContactCreateRequest;
import com.example.backend.dto.MessageResponse;
import com.example.backend.service.ContactService;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // POST /api/contacts (認証不要)
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody ContactCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(request.contact()));
    }
}

