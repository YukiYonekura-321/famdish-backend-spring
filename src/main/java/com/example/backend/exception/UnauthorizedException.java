package com.example.backend.exception;

/** HTTP 401 に対応する例外。 */
public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public UnauthorizedException(String message) {
        super(message);
    }
}
