package com.repsy.repositoryapi.exception;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        String message,
        HttpStatus httpStatus,
        long timestamp,
        String path
) {
}