package com.assignment.movieticket.integration;

import com.assignment.movieticket.domain.AppUser;
import com.assignment.movieticket.domain.Show;
import com.assignment.movieticket.dto.BookingDtos.HoldRequest;
import com.assignment.movieticket.dto.BookingDtos.HoldResponse;
import com.assignment.movieticket.dto.ShowDtos.ShowSeatResponse;
import com.assignment.movieticket.support.TestDataFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * application-test.yml sets a 2s hold TTL and a 1s sweep interval specifically so this test can
 * observe real expiry without a contrived clock.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HoldExpiryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void expiredHold_isSweptAndSeatBecomesAvailableAgain() {
        Show show = testDataFactory.createShowWithSeats(2, LocalDateTime.now().plusDays(5).withHour(21).withMinute(0));
        Long seatId = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId())
                .getBody()[0].seatId();
        AppUser customer = testDataFactory.createCustomer();
        var client = restTemplate.withBasicAuth(customer.getUsername(), "pass1234");

        client.postForEntity("/api/shows/{showId}/hold", new HoldRequest(List.of(seatId)), HoldResponse.class, show.getId());

        var heldSeat = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId())
                .getBody()[0];
        assertThat(heldSeat.status().name()).isEqualTo("HELD");

        Awaitility.await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            var seat = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId())
                    .getBody()[0];
            assertThat(seat.status().name()).isEqualTo("AVAILABLE");
        });

        // seat freed up -> a different customer can now hold it
        AppUser otherCustomer = testDataFactory.createCustomer();
        var otherClient = restTemplate.withBasicAuth(otherCustomer.getUsername(), "pass1234");
        var retryHold = otherClient.postForEntity("/api/shows/{showId}/hold", new HoldRequest(List.of(seatId)),
                HoldResponse.class, show.getId());
        assertThat(retryHold.getStatusCode().value()).isEqualTo(201);
    }
}
