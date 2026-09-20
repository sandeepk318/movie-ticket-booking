package com.assignment.movieticket.event;

public class BookingEvents {
    public record BookingConfirmedEvent(Long bookingId) {}
    public record BookingCancelledEvent(Long bookingId) {}
    public record BookingReminderEvent(Long bookingId) {}
}
