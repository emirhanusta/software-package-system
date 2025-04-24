package com.repsy.repositoryapi.exception.custom;

public class PackageAlreadyExistsException extends RuntimeException {
    public PackageAlreadyExistsException(String message) {
        super(message);
    }
}