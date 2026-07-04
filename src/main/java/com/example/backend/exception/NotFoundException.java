package com.example.backend.exception;

/** HTTP 404 に対応する例外。 */
public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
        super(message);
    }
}
