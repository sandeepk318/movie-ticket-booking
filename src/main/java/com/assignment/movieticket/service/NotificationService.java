package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.Booking;
import com.assignment.movieticket.domain.Notification;
import com.assignment.movieticket.event.BookingEvents.BookingCancelledEvent;
import com.assignment.movieticket.event.BookingEvents.BookingConfirmedEvent;
import com.assignment.movieticket.event.BookingEvents.BookingReminderEvent;
import com.assignment.movieticket.repository.BookingRepository;
import com.assignment.movieticket.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Dispatch runs on the bounded {@code notificationExecutor} pool AFTER the originating
 * transaction commits, so it never blocks (or is rolled back with) the booking/cancel request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        dispatch(event.bookingId(), Notification.Type.BOOKING_CONFIRMATION);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onBookingCancelled(BookingCancelledEvent event) {
        dispatch(event.bookingId(), Notification.Type.CANCELLATION);
    }

    @Async("notificationExecutor")
    @EventListener
    @Transactional
    public void onReminderDue(BookingReminderEvent event) {
        dispatch(event.bookingId(), Notification.Type.REMINDER);
    }

    @Transactional
    public void dispatch(Long bookingId, Notification.Type type) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return;
        }
        Notification notification = new Notification(booking, type);
        notification.setStatus(Notification.Status.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("[notification] {} -> customer={} booking={} show={}", type, booking.getCustomer().getUsername(),
                booking.getId(), booking.getShow().getMovie().getTitle());
    }
}
