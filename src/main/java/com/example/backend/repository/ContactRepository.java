package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
