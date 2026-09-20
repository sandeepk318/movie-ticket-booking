package com.assignment.movieticket.dto;

import com.assignment.movieticket.domain.DiscountCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DiscountDtos {

    public record DiscountCodeRequest(@NotBlank String code, @NotNull DiscountCode.DiscountType discountType,
                                       @NotNull BigDecimal value, @Min(1) int maxUsage,
                                       @NotNull LocalDateTime validFrom, @NotNull LocalDateTime validTo) {}

    public record DiscountCodeResponse(Long id, String code, DiscountCode.DiscountType discountType,
                                        BigDecimal value, int maxUsage, int usageCount,
                                        LocalDateTime validFrom, LocalDateTime validTo, boolean active) {}
}
