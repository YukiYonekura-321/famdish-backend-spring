package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactAttributesRequest(
	@NotBlank(message = "Name can't be blank") String name,
	@NotBlank(message = "Email can't be blank") @Email(message = "Email is invalid") String email,
	@NotBlank(message = "Subject can't be blank") String subject,
	@NotBlank(message = "Message can't be blank") String message
) {
}

