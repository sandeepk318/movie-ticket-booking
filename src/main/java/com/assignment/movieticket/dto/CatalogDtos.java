package com.assignment.movieticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class CatalogDtos {

    public record MovieRequest(@NotBlank String title, @Min(1) int durationMinutes, String language, String genre) {}
    public record MovieResponse(Long id, String title, int durationMinutes, String language, String genre) {}

    public record PricingTierRequest(
            @NotBlank String name,
            @NotNull @PositiveOrZero BigDecimal regularSeatPrice,
            @NotNull @PositiveOrZero BigDecimal premiumSeatPrice,
            @NotNull @PositiveOrZero BigDecimal weekendSurchargePercent
    ) {}
    public record PricingTierResponse(Long id, String name, BigDecimal regularSeatPrice,
                                       BigDecimal premiumSeatPrice, BigDecimal weekendSurchargePercent) {}
}
