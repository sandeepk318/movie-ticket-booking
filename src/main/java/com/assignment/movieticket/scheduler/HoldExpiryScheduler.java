package com.assignment.movieticket.scheduler;

import com.assignment.movieticket.repository.BookingHoldRepository;
import com.assignment.movieticket.service.BookingHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Safety-net sweeper: holds are also lazily treated as expired the moment any hold/confirm
 * touches them, but this guarantees seats free up even if nobody looks at that show again.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryScheduler {

    private final BookingHoldRepository bookingHoldRepository;
    private final BookingHoldService bookingHoldService;

    @Scheduled(fixedRateString = "${booking.hold-sweep-interval-ms}")
    public void sweepExpiredHolds() {
        var expired = bookingHoldRepository.findExpiredActiveHolds(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        log.debug("Sweeping {} expired hold(s)", expired.size());
        for (var hold : expired) {
            bookingHoldService.expireHoldIfStillActive(hold.getId());
        }
    }
}
