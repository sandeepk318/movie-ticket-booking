package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.RefundPolicy;
import com.assignment.movieticket.repository.RefundPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RefundPolicyServiceTest {

    @Mock
    private RefundPolicyRepository refundPolicyRepository;

    private RefundPolicyService refundPolicyService;

    private final List<RefundPolicy> policies = List.of(
            new RefundPolicy("Full", 24, BigDecimal.valueOf(100)),
            new RefundPolicy("Partial", 2, BigDecimal.valueOf(50)),
            new RefundPolicy("None", 0, BigDecimal.ZERO)
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        refundPolicyService = new RefundPolicyService(refundPolicyRepository);
        when(refundPolicyRepository.findByActiveTrueOrderByMinHoursBeforeShowDesc()).thenReturn(policies);
    }

    @Test
    void moreThan24hBeforeShow_getsFullRefund() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var resolved = refundPolicyService.resolve(now.plusHours(30), now);
        assertThat(refundPolicyService.percentFor(resolved)).isEqualByComparingTo("100");
    }

    @Test
    void between2And24h_getsPartialRefund() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var resolved = refundPolicyService.resolve(now.plusHours(5), now);
        assertThat(refundPolicyService.percentFor(resolved)).isEqualByComparingTo("50");
    }

    @Test
    void lessThan2h_getsNoRefund() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        var resolved = refundPolicyService.resolve(now.plusMinutes(30), now);
        assertThat(refundPolicyService.percentFor(resolved)).isEqualByComparingTo("0");
    }
}
