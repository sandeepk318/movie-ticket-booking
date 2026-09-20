package com.assignment.movieticket.integration;

import com.assignment.movieticket.domain.AppUser;
import com.assignment.movieticket.domain.DiscountCode;
import com.assignment.movieticket.domain.Show;
import com.assignment.movieticket.dto.BookingDtos.*;
import com.assignment.movieticket.dto.ShowDtos.ShowSeatResponse;
import com.assignment.movieticket.repository.DiscountCodeRepository;
import com.assignment.movieticket.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the discount code's usage cap is enforced atomically: with maxUsage=3 and 8 concurrent
 * redeemers on distinct seats (no seat contention involved), exactly 3 confirms may succeed with
 * the discount applied and the code's usage_count must never exceed 3.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConcurrentDiscountCodeIntegrationTest {

    private static final int REDEEMERS = 8;
    private static final int MAX_USAGE = 3;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @Test
    void discountUsageCap_neverExceededUnderConcurrentRedemption() throws InterruptedException {
        discountCodeRepository.save(new DiscountCode("RACE10", DiscountCode.DiscountType.PERCENT, BigDecimal.TEN,
                MAX_USAGE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));

        Show show = testDataFactory.createShowWithSeats(REDEEMERS, LocalDateTime.now().plusDays(4).withHour(18).withMinute(0));
        var seatMap = restTemplate.getForEntity("/api/shows/{id}/seats", ShowSeatResponse[].class, show.getId()).getBody();

        ExecutorService pool = Executors.newFixedThreadPool(REDEEMERS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(REDEEMERS);
        AtomicInteger discountedSuccessCount = new AtomicInteger();

        for (int i = 0; i < REDEEMERS; i++) {
            Long seatId = seatMap[i].seatId();
            AppUser customer = testDataFactory.createCustomer();
            pool.submit(() -> {
                try {
                    startGate.await();
                    var client = restTemplate.withBasicAuth(customer.getUsername(), "pass1234");
                    Long holdId = client.postForEntity("/api/shows/{showId}/hold", new HoldRequest(List.of(seatId)),
                            HoldResponse.class, show.getId()).getBody().holdId();
                    var confirmResp = client.postForEntity("/api/holds/{holdId}/confirm",
                            new ConfirmRequest("RACE10", "CARD"), BookingResponse.class, holdId);
                    if (confirmResp.getStatusCode() == HttpStatus.CREATED
                            && confirmResp.getBody().discountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        discountedSuccessCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean finished = doneGate.await(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(finished).isTrue();
        assertThat(discountedSuccessCount.get()).isEqualTo(MAX_USAGE);

        DiscountCode reloaded = discountCodeRepository.findByCodeIgnoreCase("RACE10").orElseThrow();
        assertThat(reloaded.getUsageCount()).isEqualTo(MAX_USAGE);
    }
}
