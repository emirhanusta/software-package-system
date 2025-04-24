package com.repsy.repositoryapi.exception.custom;

public class InvalidExtensionException extends RuntimeException {
    public InvalidExtensionException(String message) {
        super(message);
    }
}