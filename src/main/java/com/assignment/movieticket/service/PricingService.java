package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.SeatType;
import com.assignment.movieticket.domain.Show;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    /** base price for the seat type, plus the tier's weekend surcharge if the show falls on Sat/Sun. */
    public BigDecimal priceFor(Show show, SeatType seatType) {
        BigDecimal base = show.getPricingTier().basePriceFor(seatType);
        if (!show.isWeekend()) {
            return base.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal surchargeMultiplier = BigDecimal.ONE
                .add(show.getPricingTier().getWeekendSurchargePercent().divide(BigDecimal.valueOf(100)));
        return base.multiply(surchargeMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
