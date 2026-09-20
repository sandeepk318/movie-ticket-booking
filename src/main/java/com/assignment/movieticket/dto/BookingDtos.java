package com.assignment.movieticket.dto;

import com.assignment.movieticket.domain.Booking;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDtos {

    public record HoldRequest(@NotEmpty List<Long> seatIds) {}

    public record HoldResponse(Long holdId, Long showId, LocalDateTime expiresAt, List<Long> seatIds) {}

    public record ConfirmRequest(String discountCode, @NotNull String paymentMethod) {}

    public record BookingSeatView(Long showSeatId, String seatLabel, BigDecimal price) {}

    public record BookingResponse(Long id, Long showId, String movieTitle, LocalDateTime showTime,
                                   List<BookingSeatView> seats, BigDecimal baseAmount, BigDecimal discountAmount,
                                   BigDecimal finalAmount, Booking.Status status, LocalDateTime bookedAt,
                                   LocalDateTime cancelledAt) {}
}
