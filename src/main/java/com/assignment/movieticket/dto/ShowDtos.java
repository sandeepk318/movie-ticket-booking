package com.assignment.movieticket.dto;

import com.assignment.movieticket.domain.SeatType;
import com.assignment.movieticket.domain.Show;
import com.assignment.movieticket.domain.ShowSeat;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowDtos {

    public record ShowRequest(@NotNull Long movieId, @NotNull Long screenId, @NotNull Long pricingTierId,
                               @NotNull LocalDateTime showTime) {}

    public record ShowResponse(Long id, Long movieId, String movieTitle, Long screenId, String theaterName,
                                Long cityId, String cityName, LocalDateTime showTime, Show.ShowStatus status) {}

    public record ShowSeatResponse(Long showSeatId, Long seatId, String label, SeatType seatType,
                                    ShowSeat.Status status, BigDecimal price) {}
}
