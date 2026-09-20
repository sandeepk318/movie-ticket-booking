package com.assignment.movieticket.service;

import com.assignment.movieticket.config.BookingProperties;
import com.assignment.movieticket.domain.*;
import com.assignment.movieticket.dto.BookingDtos.*;
import com.assignment.movieticket.event.BookingEvents.BookingConfirmedEvent;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Owns the concurrency-critical hold/confirm lifecycle. Every method below locks the relevant
 * {@code ShowSeat} rows with {@code SELECT ... FOR UPDATE} (ordered by seat id) before reading or
 * mutating their status, inside a single transaction — see LLD.md section 4 for the correctness
 * argument.
 */
@Service
@RequiredArgsConstructor
public class BookingHoldService {

    private final ShowSeatRepository showSeatRepository;
    private final BookingHoldRepository bookingHoldRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final ShowService showService;
    private final PricingService pricingService;
    private final DiscountCodeService discountCodeService;
    private final PaymentGateway paymentGateway;
    private final BookingProperties bookingProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public HoldResponse createHold(Long showId, HoldRequest req, AppUser customer) {
        Show show = showService.getShowEntity(showId);
        if (show.getStatus() != Show.ShowStatus.SCHEDULED) {
            throw new ApiExceptions.ValidationException("Show is not open for booking: " + showId);
        }
        List<Long> seatIds = req.seatIds().stream().distinct().toList();

        List<ShowSeat> rows = showSeatRepository.lockByShowAndSeatIds(showId, seatIds);
        if (rows.size() != seatIds.size()) {
            Set<Long> found = rows.stream().map(r -> r.getSeat().getId()).collect(Collectors.toSet());
            List<Long> missing = seatIds.stream().filter(id -> !found.contains(id)).toList();
            throw new ApiExceptions.NotFoundException("Seat(s) not found for this show: " + missing);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> unavailable = rows.stream()
                .filter(row -> !isAvailableOrExpired(row, now))
                .map(row -> row.getSeat().getId())
                .toList();
        if (!unavailable.isEmpty()) {
            throw new ApiExceptions.SeatUnavailableException("Seat(s) already held or booked: " + unavailable, unavailable);
        }

        LocalDateTime expiresAt = now.plusSeconds(bookingProperties.getHoldTtlSeconds());
        BookingHold hold = bookingHoldRepository.save(new BookingHold(show, customer, expiresAt));

        for (ShowSeat row : rows) {
            row.setStatus(ShowSeat.Status.HELD);
            row.setHoldId(hold.getId());
        }
        showSeatRepository.saveAll(rows);

        return new HoldResponse(hold.getId(), show.getId(), expiresAt, seatIds);
    }

    @Transactional
    public void releaseHold(Long holdId, AppUser customer) {
        BookingHold hold = bookingHoldRepository.findById(holdId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Hold not found: " + holdId));
        assertOwnedBy(hold, customer);
        if (hold.getStatus() != BookingHold.Status.ACTIVE) {
            throw new ApiExceptions.ValidationException("Hold is not active: " + holdId);
        }
        List<ShowSeat> rows = showSeatRepository.lockByHoldId(holdId);
        freeSeats(rows);
        hold.setStatus(BookingHold.Status.RELEASED);
    }

    @Transactional
    public BookingResponse confirm(Long holdId, ConfirmRequest req, AppUser customer) {
        BookingHold hold = bookingHoldRepository.findById(holdId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Hold not found: " + holdId));
        assertOwnedBy(hold, customer);

        List<ShowSeat> rows = showSeatRepository.lockByHoldId(holdId);
        LocalDateTime now = LocalDateTime.now();

        if (hold.getStatus() != BookingHold.Status.ACTIVE || hold.isExpired(now)) {
            if (hold.getStatus() == BookingHold.Status.ACTIVE) {
                freeSeats(rows);
                hold.setStatus(BookingHold.Status.EXPIRED);
            }
            throw new ApiExceptions.HoldExpiredException("Hold has expired or is no longer active: " + holdId);
        }

        Show show = hold.getShow();
        BigDecimal baseAmount = rows.stream()
                .map(row -> pricingService.priceFor(show, row.getSeat().getSeatType()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        DiscountCode discountCode = null;
        if (req.discountCode() != null && !req.discountCode().isBlank()) {
            var applied = discountCodeService.applyAndLock(req.discountCode(), baseAmount, now);
            discountCode = applied.discountCode();
            discountAmount = applied.discountAmount();
        }
        BigDecimal finalAmount = baseAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        var paymentResult = paymentGateway.charge(finalAmount, req.paymentMethod());
        if (!paymentResult.success()) {
            throw new ApiExceptions.PaymentFailedException(paymentResult.message());
        }

        Booking booking = new Booking();
        booking.setHold(hold);
        booking.setShow(show);
        booking.setCustomer(customer);
        booking.setDiscountCode(discountCode);
        booking.setBaseAmount(baseAmount);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking = bookingRepository.save(booking);

        List<BookingSeatView> seatViews = new ArrayList<>();
        for (ShowSeat row : rows) {
            row.setStatus(ShowSeat.Status.BOOKED);
            BigDecimal price = pricingService.priceFor(show, row.getSeat().getSeatType());
            bookingSeatRepository.save(new BookingSeat(booking, row, price));
            seatViews.add(new BookingSeatView(row.getId(), row.getSeat().label(), price));
        }
        showSeatRepository.saveAll(rows);

        paymentRepository.save(new Payment(booking, finalAmount, req.paymentMethod(), Payment.Status.SUCCESS, paymentResult.transactionRef()));

        hold.setStatus(BookingHold.Status.CONFIRMED);

        eventPublisher.publishEvent(new BookingConfirmedEvent(booking.getId()));

        return new BookingResponse(booking.getId(), show.getId(), show.getMovie().getTitle(), show.getShowTime(),
                seatViews, baseAmount, discountAmount, finalAmount, booking.getStatus(), booking.getBookedAt(), null);
    }

    /** Used by the expiry sweeper: each expired hold is processed in its own transaction. */
    @Transactional
    public void expireHoldIfStillActive(Long holdId) {
        BookingHold hold = bookingHoldRepository.findById(holdId).orElse(null);
        if (hold == null || hold.getStatus() != BookingHold.Status.ACTIVE) {
            return;
        }
        List<ShowSeat> rows = showSeatRepository.lockByHoldId(holdId);
        if (!hold.isExpired(LocalDateTime.now())) {
            return; // raced with a confirm/release between the scheduler's read and this lock
        }
        freeSeats(rows);
        hold.setStatus(BookingHold.Status.EXPIRED);
    }

    private boolean isAvailableOrExpired(ShowSeat row, LocalDateTime now) {
        if (row.getStatus() == ShowSeat.Status.AVAILABLE) {
            return true;
        }
        if (row.getStatus() == ShowSeat.Status.HELD && row.getHoldId() != null) {
            return bookingHoldRepository.findById(row.getHoldId())
                    .map(h -> h.isExpired(now))
                    .orElse(true);
        }
        return false;
    }

    private void freeSeats(List<ShowSeat> rows) {
        for (ShowSeat row : rows) {
            row.setStatus(ShowSeat.Status.AVAILABLE);
            row.setHoldId(null);
        }
        showSeatRepository.saveAll(rows);
    }

    private void assertOwnedBy(BookingHold hold, AppUser customer) {
        if (!hold.getCustomer().getId().equals(customer.getId()) && customer.getRole() != AppUser.Role.ADMIN) {
            throw new ApiExceptions.ForbiddenException("Hold does not belong to the current user");
        }
    }
}
