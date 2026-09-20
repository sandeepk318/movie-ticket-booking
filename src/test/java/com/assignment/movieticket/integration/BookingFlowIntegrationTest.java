package com.assignment.movieticket.integration;

import com.assignment.movieticket.domain.*;
import com.assignment.movieticket.dto.BookingDtos.*;
import com.assignment.movieticket.dto.ShowDtos.ShowSeatResponse;
import com.assignment.movieticket.repository.DiscountCodeRepository;
import com.assignment.movieticket.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    private Show show;
    private AppUser customer;
    private List<Long> seatIds;

    @BeforeEach
    void setUp() {
        show = testDataFactory.createShowWithSeats(10, LocalDateTime.now().plusDays(3).withHour(19).withMinute(0));
        customer = testDataFactory.createCustomer();

        var seatMapResponse = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId());
        seatIds = List.of(seatMapResponse.getBody()[0].seatId(), seatMapResponse.getBody()[1].seatId());
    }

    private TestRestTemplate asCustomer() {
        return restTemplate.withBasicAuth(customer.getUsername(), "pass1234");
    }

    private TestRestTemplate asAdmin() {
        return restTemplate.withBasicAuth("admin", "admin123");
    }

    @Test
    void holdConfirmAndCancel_happyPath() {
        ResponseEntity<HoldResponse> holdResp = asCustomer().postForEntity(
                "/api/shows/{showId}/hold", new HoldRequest(seatIds), HoldResponse.class, show.getId());
        assertThat(holdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long holdId = holdResp.getBody().holdId();

        ResponseEntity<BookingResponse> confirmResp = asCustomer().postForEntity(
                "/api/holds/{holdId}/confirm", new ConfirmRequest(null, "CARD"), BookingResponse.class, holdId);
        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BookingResponse booking = confirmResp.getBody();
        assertThat(booking.status()).isEqualTo(Booking.Status.CONFIRMED);
        assertThat(booking.finalAmount()).isEqualByComparingTo(booking.baseAmount());

        ResponseEntity<BookingResponse[]> listResp = asCustomer().getForEntity("/api/bookings", BookingResponse[].class);
        assertThat(listResp.getBody()).extracting(BookingResponse::id).contains(booking.id());

        ResponseEntity<BookingResponse> cancelResp = asCustomer().postForEntity(
                "/api/bookings/{id}/cancel", null, BookingResponse.class, booking.id());
        assertThat(cancelResp.getBody().status()).isEqualTo(Booking.Status.CANCELLED);

        // seats should be free again after cancellation
        var seatMapAfter = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId());
        boolean anyStillBooked = List.of(seatMapAfter.getBody()).stream()
                .filter(s -> seatIds.contains(s.seatId()))
                .anyMatch(s -> s.status() != ShowSeat.Status.AVAILABLE);
        assertThat(anyStillBooked).isFalse();
    }

    @Test
    void discountCode_reducesFinalAmount() {
        discountCodeRepository.save(new DiscountCode("TESTCODE", DiscountCode.DiscountType.PERCENT, BigDecimal.TEN,
                10, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));

        Long holdId = asCustomer().postForEntity("/api/shows/{showId}/hold", new HoldRequest(seatIds),
                HoldResponse.class, show.getId()).getBody().holdId();

        ResponseEntity<BookingResponse> confirmResp = asCustomer().postForEntity(
                "/api/holds/{holdId}/confirm", new ConfirmRequest("TESTCODE", "CARD"), BookingResponse.class, holdId);

        BookingResponse booking = confirmResp.getBody();
        assertThat(booking.discountAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(booking.finalAmount()).isEqualByComparingTo(booking.baseAmount().subtract(booking.discountAmount()));
    }

    @Test
    void holdingAlreadyHeldSeat_returns409() {
        asCustomer().postForEntity("/api/shows/{showId}/hold", new HoldRequest(seatIds), HoldResponse.class, show.getId());

        AppUser otherCustomer = testDataFactory.createCustomer();
        ResponseEntity<String> secondHold = restTemplate.withBasicAuth(otherCustomer.getUsername(), "pass1234")
                .postForEntity("/api/shows/{showId}/hold", new HoldRequest(seatIds), String.class, show.getId());

        assertThat(secondHold.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void customerCannotAccessAdminEndpoints() {
        ResponseEntity<String> resp = asCustomer().getForEntity("/api/admin/cities", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCannotAccessCustomerBookingEndpoints() {
        ResponseEntity<String> resp = asAdmin().getForEntity("/api/bookings", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
