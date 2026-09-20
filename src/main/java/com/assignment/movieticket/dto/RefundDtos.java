package com.assignment.movieticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RefundDtos {

    public record RefundPolicyRequest(@NotBlank String name, @Min(0) int minHoursBeforeShow,
                                       @NotNull BigDecimal refundPercent) {}

    public record RefundPolicyResponse(Long id, String name, int minHoursBeforeShow,
                                        BigDecimal refundPercent, boolean active) {}
}
