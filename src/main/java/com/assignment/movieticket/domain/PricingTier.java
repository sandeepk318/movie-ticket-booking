package com.assignment.movieticket.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_tier")
@Getter
@Setter
@NoArgsConstructor
public class PricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "regular_seat_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal regularSeatPrice;

    @Column(name = "premium_seat_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumSeatPrice;

    @Column(name = "weekend_surcharge_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal weekendSurchargePercent = BigDecimal.ZERO;

    public PricingTier(String name, BigDecimal regularSeatPrice, BigDecimal premiumSeatPrice, BigDecimal weekendSurchargePercent) {
        this.name = name;
        this.regularSeatPrice = regularSeatPrice;
        this.premiumSeatPrice = premiumSeatPrice;
        this.weekendSurchargePercent = weekendSurchargePercent;
    }

    public BigDecimal basePriceFor(SeatType seatType) {
        return seatType == SeatType.PREMIUM ? premiumSeatPrice : regularSeatPrice;
    }
}
