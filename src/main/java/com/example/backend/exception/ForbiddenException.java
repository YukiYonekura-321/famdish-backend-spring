package com.example.backend.exception;

/** HTTP 403 に対応する例外。 */
public class ForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public ForbiddenException(String message) {
        super(message);
    }
}
