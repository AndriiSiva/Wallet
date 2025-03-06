package com.wallet.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleWalletNotFound(WalletNotFoundException ex) {
        return Mono.just(createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleInsufficientFunds(InsufficientFundsException ex) {
        return Mono.just(ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage())));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = "Invalid data: " + ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Mono.just(createErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler({ServerWebInputException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> handleInvalidJson(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid JSON"));
    }



    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleGenericError(RuntimeException ex) {
        return Mono.just(createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return new ResponseEntity<>(response, status);
    }
}