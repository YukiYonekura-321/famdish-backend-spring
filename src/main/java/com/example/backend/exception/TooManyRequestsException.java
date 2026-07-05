package com.example.backend.exception;

/** HTTP 429 に対応する例外（トライアル利用回数超過など）。 */
public class TooManyRequestsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public TooManyRequestsException(String message) {
        super(message);
    }
}
