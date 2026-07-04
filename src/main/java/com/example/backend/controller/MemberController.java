package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.IdNameResponse;
import com.example.backend.dto.MemberCreateRequest;
import com.example.backend.dto.MemberCreateResponse;
import com.example.backend.dto.MemberMeResponse;
import com.example.backend.dto.MemberSummaryResponse;
import com.example.backend.dto.MemberUpdateRequest;
import com.example.backend.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // GET /api/members
    @GetMapping
    public ResponseEntity<List<MemberSummaryResponse>> index() {
        return ResponseEntity.ok(memberService.index());
    }

    // POST /api/members
    @PostMapping
    public ResponseEntity<MemberCreateResponse> create(@RequestBody MemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    // PATCH /api/members/:id
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody MemberUpdateRequest request) {
        memberService.update(id, request.member());
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/members/:id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        memberService.destroy(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/members/me
    @GetMapping("/me")
    public ResponseEntity<MemberMeResponse> me() {
        return ResponseEntity.ok(memberService.me());
    }

    // GET /api/members/all
    @GetMapping("/all")
    public ResponseEntity<List<IdNameResponse>> all() {
        return ResponseEntity.ok(memberService.all());
    }
}
