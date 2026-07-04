package com.example.backend.exception;

import java.util.List;

/**
 * HTTP 422 のバリデーションエラー (Rails の record.errors.full_messages 配列相当)。
 * レスポンスは {"errors": [...]} の形になる。
 */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}

