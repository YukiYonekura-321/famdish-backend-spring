package com.example.backend.exception;

/** HTTP 400 に対応する例外。 */
public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
        super(message);
    }
}
