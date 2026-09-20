package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.DiscountCode;
import com.assignment.movieticket.exception.ApiExceptions;
import com.assignment.movieticket.repository.DiscountCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class DiscountCodeServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    private DiscountCodeService discountCodeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        discountCodeService = new DiscountCodeService(discountCodeRepository);
    }

    @Test
    void percentCode_computesDiscountAndBumpsUsage() {
        DiscountCode code = new DiscountCode("SAVE10", DiscountCode.DiscountType.PERCENT, BigDecimal.TEN, 5,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(discountCodeRepository.lockByCode("SAVE10")).thenReturn(Optional.of(code));

        var applied = discountCodeService.applyAndLock("save10", BigDecimal.valueOf(1000), LocalDateTime.now());

        assertThat(applied.discountAmount()).isEqualByComparingTo("100.00");
        assertThat(code.getUsageCount()).isEqualTo(1);
    }

    @Test
    void flatCode_neverDiscountsMoreThanBaseAmount() {
        DiscountCode code = new DiscountCode("FLAT500", DiscountCode.DiscountType.FLAT, BigDecimal.valueOf(500), 5,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(discountCodeRepository.lockByCode("FLAT500")).thenReturn(Optional.of(code));

        var applied = discountCodeService.applyAndLock("FLAT500", BigDecimal.valueOf(300), LocalDateTime.now());

        assertThat(applied.discountAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    void exhaustedCode_isRejected() {
        DiscountCode code = new DiscountCode("USEDUP", DiscountCode.DiscountType.PERCENT, BigDecimal.TEN, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        code.setUsageCount(1);
        when(discountCodeRepository.lockByCode("USEDUP")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> discountCodeService.applyAndLock("USEDUP", BigDecimal.valueOf(100), LocalDateTime.now()))
                .isInstanceOf(ApiExceptions.ValidationException.class);
    }

    @Test
    void unknownCode_isRejected() {
        when(discountCodeRepository.lockByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountCodeService.applyAndLock("NOPE", BigDecimal.valueOf(100), LocalDateTime.now()))
                .isInstanceOf(ApiExceptions.ValidationException.class);
    }
}
