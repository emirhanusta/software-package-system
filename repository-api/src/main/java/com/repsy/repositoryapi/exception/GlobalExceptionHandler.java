package com.repsy.repositoryapi.exception;

import com.repsy.storagecore.exception.DirectoryCreationException;
import com.repsy.storagecore.exception.MetaDataMismatchException;
import com.repsy.storagecore.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(ResourceNotFoundException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.NOT_FOUND, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.BAD_REQUEST, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DirectoryCreationException.class)
    public ResponseEntity<ErrorResponse> handleDirectoryCreationException(DirectoryCreationException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MetaDataMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMetaDataException(MetaDataMismatchException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.BAD_REQUEST, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }
}
