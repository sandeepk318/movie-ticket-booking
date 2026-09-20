package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.*;
import com.assignment.movieticket.dto.BookingDtos.*;
import com.assignment.movieticket.event.BookingEvents.BookingCancelledEvent;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final RefundPolicyService refundPolicyService;
    private final RefundRepository refundRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<BookingResponse> listMine(AppUser customer) {
        return bookingRepository.findByCustomerIdOrderByBookedAtDesc(customer.getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, AppUser requester) {
        Booking booking = getEntity(id);
        assertOwnedBy(booking, requester);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(Long id, AppUser requester) {
        Booking booking = getEntity(id);
        assertOwnedBy(booking, requester);
        if (booking.getStatus() != Booking.Status.CONFIRMED) {
            throw new ApiExceptions.ValidationException("Only confirmed bookings can be cancelled");
        }

        List<Long> showSeatIds = bookingSeatRepository.findByBookingId(booking.getId()).stream()
                .map(bs -> bs.getShowSeat().getId()).toList();
        List<ShowSeat> rows = showSeatRepository.lockByIds(showSeatIds);
        for (ShowSeat row : rows) {
            row.setStatus(ShowSeat.Status.AVAILABLE);
            row.setHoldId(null);
        }
        showSeatRepository.saveAll(rows);

        LocalDateTime now = LocalDateTime.now();
        var policy = refundPolicyService.resolve(booking.getShow().getShowTime(), now);
        BigDecimal refundPercent = refundPolicyService.percentFor(policy);
        BigDecimal refundAmount = booking.getFinalAmount()
                .multiply(refundPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        refundRepository.save(new Refund(booking, policy.orElse(null), refundAmount, Refund.Status.PROCESSED));

        booking.setStatus(Booking.Status.CANCELLED);
        booking.setCancelledAt(now);

        eventPublisher.publishEvent(new BookingCancelledEvent(booking.getId()));

        return toResponse(booking);
    }

    private Booking getEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Booking not found: " + id));
    }

    private void assertOwnedBy(Booking booking, AppUser requester) {
        if (!booking.getCustomer().getId().equals(requester.getId()) && requester.getRole() != AppUser.Role.ADMIN) {
            throw new ApiExceptions.ForbiddenException("Booking does not belong to the current user");
        }
    }

    private BookingResponse toResponse(Booking booking) {
        List<BookingSeatView> seats = bookingSeatRepository.findByBookingId(booking.getId()).stream()
                .map(bs -> new BookingSeatView(bs.getShowSeat().getId(), bs.getShowSeat().getSeat().label(), bs.getPriceAtBooking()))
                .toList();
        return new BookingResponse(booking.getId(), booking.getShow().getId(), booking.getShow().getMovie().getTitle(),
                booking.getShow().getShowTime(), seats, booking.getBaseAmount(), booking.getDiscountAmount(),
                booking.getFinalAmount(), booking.getStatus(), booking.getBookedAt(), booking.getCancelledAt());
    }
}
