package com.assignment.movieticket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public class ApiExceptions {

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }

    /** Thrown when one or more requested seats can't be held (already HELD/BOOKED). */
    public static class SeatUnavailableException extends RuntimeException {
        private final List<Long> unavailableSeatIds;
        public SeatUnavailableException(String message, List<Long> unavailableSeatIds) {
            super(message);
            this.unavailableSeatIds = unavailableSeatIds;
        }
        public List<Long> getUnavailableSeatIds() { return unavailableSeatIds; }
    }

    public static class HoldExpiredException extends RuntimeException {
        public HoldExpiredException(String message) { super(message); }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) { super(message); }
    }

    public static class PaymentFailedException extends RuntimeException {
        public PaymentFailedException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ConcurrentUpdateException extends RuntimeException {
        public ConcurrentUpdateException(String message) { super(message); }
    }
}
