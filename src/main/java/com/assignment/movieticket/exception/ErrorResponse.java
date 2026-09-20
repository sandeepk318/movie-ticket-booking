package com.assignment.movieticket.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                             String path, Map<String, Object> details) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, Map.of());
    }

    public static ErrorResponse withUnavailableSeats(int status, String error, String message, String path,
                                                       List<Long> unavailableSeatIds) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path,
                Map.of("unavailableSeatIds", unavailableSeatIds));
    }
}
