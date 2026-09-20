package com.assignment.movieticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_code")
@Getter
@Setter
@NoArgsConstructor
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 16)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "max_usage", nullable = false)
    private Integer maxUsage;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public enum DiscountType { PERCENT, FLAT }

    public DiscountCode(String code, DiscountType discountType, BigDecimal value, Integer maxUsage,
                         LocalDateTime validFrom, LocalDateTime validTo) {
        this.code = code;
        this.discountType = discountType;
        this.value = value;
        this.maxUsage = maxUsage;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public boolean isUsable(LocalDateTime at) {
        return active && !at.isBefore(validFrom) && !at.isAfter(validTo) && usageCount < maxUsage;
    }
}
