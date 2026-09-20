package com.assignment.movieticket.integration;

import com.assignment.movieticket.domain.AppUser;
import com.assignment.movieticket.domain.Show;
import com.assignment.movieticket.dto.BookingDtos.HoldRequest;
import com.assignment.movieticket.dto.ShowDtos.ShowSeatResponse;
import com.assignment.movieticket.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The headline correctness test: N customers race to hold the exact same seat on the exact same
 * show at the exact same instant. Exactly one must win (201); everyone else must get a clean 409
 * SEAT_UNAVAILABLE, never a double allocation and never a lost/ambiguous result.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConcurrentSeatBookingIntegrationTest {

    private static final int CONTENDERS = 12;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void onlyOneConcurrentHold_winsTheSameSeat() throws InterruptedException {
        Show show = testDataFactory.createShowWithSeats(1, LocalDateTime.now().plusDays(2).withHour(20).withMinute(0));
        Long seatId = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId())
                .getBody()[0].seatId();

        List<AppUser> customers = java.util.stream.IntStream.range(0, CONTENDERS)
                .mapToObj(i -> testDataFactory.createCustomer())
                .toList();

        ExecutorService pool = Executors.newFixedThreadPool(CONTENDERS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(CONTENDERS);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        customers.forEach(customer -> pool.submit(() -> {
            try {
                startGate.await();
                var client = restTemplate.withBasicAuth(customer.getUsername(), "pass1234");
                var response = client.postForEntity("/api/shows/{showId}/hold", new HoldRequest(List.of(seatId)),
                        String.class, show.getId());
                if (response.getStatusCode() == HttpStatus.CREATED) {
                    successCount.incrementAndGet();
                } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    conflictCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneGate.countDown();
            }
        }));

        startGate.countDown();
        boolean finished = doneGate.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(CONTENDERS - 1);

        var seatMapAfter = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId());
        assertThat(seatMapAfter.getBody()[0].status().name()).isEqualTo("HELD");
    }
}
