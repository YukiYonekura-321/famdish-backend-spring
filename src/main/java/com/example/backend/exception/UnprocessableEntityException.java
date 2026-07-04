package com.example.backend.exception;

/** HTTP 422 に対応する例外（バリデーションエラーなど）。 */
public class UnprocessableEntityException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public UnprocessableEntityException(String message) {
        super(message);
    }
}
