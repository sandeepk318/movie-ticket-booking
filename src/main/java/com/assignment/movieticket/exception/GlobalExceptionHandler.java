package com.assignment.movieticket.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiExceptions.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ApiExceptions.NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({ApiExceptions.ValidationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(ApiExceptions.SeatUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleSeatUnavailable(ApiExceptions.SeatUnavailableException ex,
                                                                HttpServletRequest req) {
        var body = ErrorResponse.withUnavailableSeats(HttpStatus.CONFLICT.value(), "SEAT_UNAVAILABLE",
                ex.getMessage(), req.getRequestURI(), ex.getUnavailableSeatIds());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ApiExceptions.HoldExpiredException.class)
    public ResponseEntity<ErrorResponse> handleHoldExpired(ApiExceptions.HoldExpiredException ex, HttpServletRequest req) {
        return build(HttpStatus.GONE, "HOLD_EXPIRED", ex.getMessage(), req);
    }

    @ExceptionHandler({ApiExceptions.ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), req);
    }

    @ExceptionHandler(ApiExceptions.PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(ApiExceptions.PaymentFailedException ex, HttpServletRequest req) {
        return build(HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED", ex.getMessage(), req);
    }

    @ExceptionHandler({ApiExceptions.ConcurrentUpdateException.class, OptimisticLockException.class,
            OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "CONCURRENT_UPDATE",
                "The resource was modified concurrently, please retry.", req);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuth(UsernameNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        var body = new ErrorResponse(java.time.LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", "Request validation failed", req.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), error, message, req.getRequestURI()));
    }
}
