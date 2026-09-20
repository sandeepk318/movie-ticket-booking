package com.assignment.movieticket.scheduler;

import com.assignment.movieticket.config.BookingProperties;
import com.assignment.movieticket.event.BookingEvents.BookingReminderEvent;
import com.assignment.movieticket.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRateString = "${booking.reminder-scan-interval-ms}")
    @Transactional(readOnly = true)
    public void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime leadWindowEnd = now.plusHours(bookingProperties.getReminderLeadHours());
        bookingRepository.findConfirmedNeedingReminder(now, leadWindowEnd)
                .forEach(b -> eventPublisher.publishEvent(new BookingReminderEvent(b.getId())));
    }
}
