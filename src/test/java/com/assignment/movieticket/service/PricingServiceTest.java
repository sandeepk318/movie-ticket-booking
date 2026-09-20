package com.assignment.movieticket.service;

import com.assignment.movieticket.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    private Show showAt(LocalDateTime time) {
        PricingTier tier = new PricingTier("Standard", BigDecimal.valueOf(200), BigDecimal.valueOf(350), BigDecimal.valueOf(20));
        Screen screen = new Screen();
        Show show = new Show();
        show.setPricingTier(tier);
        show.setShowTime(time);
        return show;
    }

    @Test
    void weekdayShow_usesBasePriceWithNoSurcharge() {
        Show weekday = showAt(LocalDateTime.of(2026, 9, 22, 19, 0)); // Tuesday
        assertThat(pricingService.priceFor(weekday, SeatType.REGULAR)).isEqualByComparingTo("200.00");
        assertThat(pricingService.priceFor(weekday, SeatType.PREMIUM)).isEqualByComparingTo("350.00");
    }

    @Test
    void weekendShow_appliesSurcharge() {
        Show saturday = showAt(LocalDateTime.of(2026, 9, 26, 19, 0)); // Saturday
        // 200 * 1.20 = 240.00
        assertThat(pricingService.priceFor(saturday, SeatType.REGULAR)).isEqualByComparingTo("240.00");
        // 350 * 1.20 = 420.00
        assertThat(pricingService.priceFor(saturday, SeatType.PREMIUM)).isEqualByComparingTo("420.00");
    }

    @Test
    void sundayShow_alsoTreatedAsWeekend() {
        Show sunday = showAt(LocalDateTime.of(2026, 9, 27, 19, 0));
        assertThat(pricingService.priceFor(sunday, SeatType.REGULAR)).isEqualByComparingTo("240.00");
    }
}
